package com.url_shortener.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.url_shortener.CustomRandomGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;


// an annotation to create Rest APis
@RestController
@Validated
public class UserController {
    private static final int ROLE_TOKEN_LENGTH = 32;

    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final CustomRandomGenerator generator;

    @Autowired
    public UserController(CompanyRepository companyRepo,
                          UserRepository userRepo,
                          CustomRandomGenerator generator) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.generator = generator;

    }

    @Bean("UserControllerEncoder")
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


    @PostMapping("api/auth/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
        if (this.companyRepo.existsById(req.id())) {
            throw new ExistingCompanyException("There is already a company with the given id.");
        }

        // create the role tokens
        HashMap<String, String> roleTokens = new HashMap<>();

        // add the owner role token
        roleTokens.put(Owner.role(), this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the admin role token
        roleTokens.put(Admin.role(), this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the registeredUser role token
        roleTokens.put(RegisteredUser.role(), this.generator.randomString(ROLE_TOKEN_LENGTH));

        // build the company object
        Company newCompany = new Company(req.id(), req.site(), roleTokens, this.encoder());

        // save it to the database
        this.companyRepo.save(newCompany);

        return new ResponseEntity<>(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(newCompany),
                HttpStatus.CREATED);
    }

    @PostMapping("api/auth/register/user")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequest req) throws JsonProcessingException {

        if (! this.companyRepo.existsById(req.companyId())) {
            throw new UserWithNoCompanyException("No registered company with the given id: " + req.companyId());
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

        List<AppUser> companyOwner = this.userRepo.findRolesInCompany(company.getId(), claimedRole);

        System.out.println("\n" + companyOwner + "\n");

        if (companyOwner.isEmpty() && ! claimedRole.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new UserBeforeOwnerException("No user can be created before creating the OWNER user!!");
        }

        if ((!companyOwner.isEmpty()) && claimedRole.equalsIgnoreCase(RoleManager.OWNER_ROLE)) {
            throw new MultipleOwnersException("The user for the given company was already created");
        }

        // make sure the user is authenticated: has the right token for the right role:
        // use the password encoder to verify the roleToken
        if (! encoder().matches(req.roleToken(), company.getRoleTokens().get(claimedRole))) {
            throw new IncorrectRoleTokenException("The role token is incorrect");
        }

        // at this point we are ready to create the User
        AppUser newUser = new AppUser(req.username(), req.password(), company, RoleManager.getRole(claimedRole));

        this.userRepo.save(newUser);

        return ResponseEntity.ok("token role authentication done correctly !!");
    }

}
