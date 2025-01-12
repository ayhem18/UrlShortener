package com.url_shortener.Service.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.url_shortener.Service.*;
import com.url_shortener.Service.Company.Company;
import com.url_shortener.Service.Company.CompanyRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// an annotation to create Rest APis
@RestController
@Validated
public class UserController {
    private static final int ROLE_TOKEN_LENGTH = 32;

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

    @GetMapping("api/auth/users/{companyId}")
    public ResponseEntity<String> usersInCompany(@PathVariable String companyId) throws JsonProcessingException {
        List<AppUser> users = this.userRepo.findByCompany(this.companyRepo.findById(companyId).get());

        return ResponseEntity.ok(this.objectMapper().writeValueAsString(users));
    }

    @PostMapping("api/auth/register/user")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequest req) throws JsonProcessingException {

        if (! this.companyRepo.existsById(req.companyId())) {
            throw new UserWithNoCompanyException("No registered company with the given id: " + req.companyId());
        }

        if (this.userRepo.existsById(req.username())) {
            throw new AlreadyExistingUserException("The username is already register");
        }
        String claimedRole = req.role().toLowerCase();

        if (!RoleManager.ROLES_STRING.contains(claimedRole)) {
            throw new UndefinedRoleException("The claimed role " + claimedRole + " is not yet supported.");
        }

        // at this point we know the company id is in the database and the role is indeed supported.
        // however, the following security measures should be carried out:

        // at this point we know the company id is valid
        // extract the company object
        Company company = this.companyRepo.findById(req.companyId()).get();

        System.out.println(company);

        // 1. if the company has no "OWNER" user, then it has to be created first: in other words, if the role is not OWNER
        // the request should be rejected

        // 2. if there is an OWNER already, then the request should be rejected too
        List<AppUser> companyOwner = this.userRepo.findByCompanyAndRole(company, RoleManager.getRole(RoleManager.OWNER_ROLE));

        System.out.println("\n" + companyOwner + "\n");

        if (companyOwner.isEmpty() && ! claimedRole.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new UserBeforeOwnerException("No user can be created before creating the OWNER user!!");
        }

        if ((!companyOwner.isEmpty()) && claimedRole.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new MultipleOwnersException("The user for the given company was already created");
        }

        // make sure the user is authenticated: has the right token for the right role:
        // use the password encoder to verify the roleToken
        if (! encoder().matches(req.roleToken(), company.getTokens().get(claimedRole))) {
            throw new IncorrectRoleTokenException("The role token is incorrect");
        }

        // at this point we are ready to create the User
        AppUser newUser = new AppUser(req.username(), this.encoder().encode(req.password()), company, RoleManager.getRole(claimedRole));

        this.userRepo.save(newUser);

        return ResponseEntity.ok(this.objectMapper().writeValueAsString(newUser));
    }

}
