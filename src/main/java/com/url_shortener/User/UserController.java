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
    private final CustomRandomGenerator generator;

    @Autowired
    public UserController(CompanyRepository companyRepo, CustomRandomGenerator generator) {
        this.companyRepo = companyRepo;
        this.generator = generator;
    }

    @Bean("UserControllerEncoder")
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


    private List<String> roleStrings() {
        return List.of(Admin.role(), Owner.role(), RegisteredUser.role());
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

        if (!this.roleStrings().contains(claimedRole)) {
            throw new UndefinedRoleException("The claimed role " + claimedRole + " is not yet supported.");
        }

        // at this point we know the company id is valid
        // extract the company object
        Company company = this.companyRepo.findById(req.companyId()).get();

//        System.out.println("\n" + company.getRoleTokens().get(claimedRole) + "\n");
//        System.out.println("\n" + req.roleToken() + "\n");
//

        // make sure the user is authenticated: has the right token for the right role:
        // use the password encoder to verify the roleToken
        if (! encoder().matches(req.roleToken(), company.getRoleTokens().get(claimedRole))) {
            throw new IncorrectRoleTokenException("The role token is incorrect");
        }

        return ResponseEntity.ok("token role authentication done correctly !!");
    }

}
