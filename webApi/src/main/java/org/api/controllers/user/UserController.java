package org.api.controllers.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.api.exceptions.UserExceptions;
import org.api.requests.UserRegisterRequest;
import org.common.Role;
import org.common.RoleManager;
import org.data.entities.AppUser;
import org.data.entities.Company;
import org.data.entities.CompanyWrapper;
import org.data.repositories.CompanyRepository;
import org.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Validated
public class UserController {
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;

    @Autowired
    public UserController(CompanyRepository companyRepo,
                          UserRepository userRepo) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
    }

    @Bean("userControllerEncoder")
    @Primary
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter();
        return om;
    }

    @PostMapping("api/auth/register/user")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequest req) throws JsonProcessingException {

        if (! this.companyRepo.existsById(req.companyId())) {
            throw new UserExceptions.UserWithNoCompanyException("No registered company with the given id: " + req.companyId());
        }

        if (this.userRepo.existsById(req.username())) {
            throw new UserExceptions.AlreadyExistingUserException("The username already exists");
        }

        String claimedRoleStr = req.role().toLowerCase();
        // the RoleManager.getRole throws an exception if the role does not exist ...
        Role claimedRole = RoleManager.getRole(claimedRoleStr);

        // at this point, the company id and the role are both valid
        // however, the following security measures should be carried out:

        // at this point we know the company id is valid
        // extract the company object
        Company company = this.companyRepo.findById(req.companyId()).get();
        CompanyWrapper wrapper = new CompanyWrapper(company);

        // 1. if the company has no "OWNER" user, then it has to be created first: in other words, if the role is not OWNER
        // the request should be rejected

        // 2. if there is an OWNER already, then the request should be rejected too
        List<AppUser> companyOwner = this.userRepo.findByCompanyAndRole(company, RoleManager.getRole(RoleManager.OWNER_ROLE));


        if (companyOwner.isEmpty() && ! claimedRoleStr.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new UserExceptions.UserBeforeOwnerException("No user can be created before creating the OWNER user!!");
        }

        if ((!companyOwner.isEmpty()) && claimedRoleStr.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new UserExceptions.MultipleOwnersException("The user for the given company was already created");
        }

        // make sure the user is authenticated: has the right token for the right role:
        // use the password encoder to verify the roleToken
        if (! encoder().matches(req.roleToken(), wrapper.getTokens().get(claimedRoleStr))) {
            throw new UserExceptions.IncorrectRoleTokenException("The role token is incorrect");
        }

        // at this point we are ready to create the User
        AppUser newUser = new AppUser(req.username(), this.encoder().encode(req.password()), company, claimedRole);

        this.userRepo.save(newUser);

        return ResponseEntity.ok(this.objectMapper().writeValueAsString(newUser));
    }

}
