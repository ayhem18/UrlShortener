package com.url_shortener.Service.Company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.url_shortener.CustomRandomGenerator;
import com.url_shortener.Service.*;
import com.url_shortener.Service.User.AppUser;
import com.url_shortener.Service.User.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


class NoCompanyException extends RuntimeException {
    public NoCompanyException(String message) {
        super(message);
    }
}


@RestController
@Validated
public class CompanyController {
    private static final int ROLE_TOKEN_LENGTH = 32;

    private final CompanyRepository companyRepo;
    private final CustomRandomGenerator generator;
    private final UserRepository userRepo;

    @Autowired
    public CompanyController(CompanyRepository companyRepo,
                          UserRepository userRepo,
                          CustomRandomGenerator generator) {
        this.companyRepo = companyRepo;
        this.generator = generator;
        this.userRepo = userRepo;

    }

    @Bean("companyControllerEncoder")
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter();
        return om;
    }

    @PostMapping("api/auth/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {

        if (this.companyRepo.existsById(req.id())) {
            throw new ExistingCompanyException("There is already a company with the given id.");
        }

        // create the role tokens
        HashMap<String, String> roleTokens = new HashMap<>();

        // add the owner role token
        roleTokens.put(RoleManager.OWNER_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the admin role token
        roleTokens.put(RoleManager.ADMIN_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the registeredUser role token
        roleTokens.put(RoleManager.REGISTERED_USER_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // build the company object
        Company newCompany = new Company(req.id(), req.site(), roleTokens, this.encoder());

        // make sure to call the serialize first, so that the "serializeSensitiveCount" field will be saved as "4"
        // in the database preventing the serialization of sensitive information beyond the very first time
        String companySerialized = this.objectMapper().writeValueAsString(newCompany);

        // save it to the database
        this.companyRepo.save(newCompany);

        return new ResponseEntity<>(companySerialized,
                HttpStatus.CREATED);
    }

    @DeleteMapping("api/company/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable String companyId,
                                                @AuthenticationPrincipal UserDetails currentUserDetails) throws RuntimeException{

        Optional<Company> company = this.companyRepo.findById(companyId);

        if (company.isEmpty()) {
            throw new NoCompanyException("There is no company with the given Id");
        }


        // this function can be called by the Owner user of the company
        AppUser currentUser = this.userRepo.findById(currentUserDetails.getUsername()).get();


        if (!currentUser.getRole().toString().equals(RoleManager.OWNER_ROLE)) {
            throw new RuntimeException("Man you messed up the authentication");
        }

        // delete all users in the given company
        this.userRepo.deleteByCompany(company.get());

        // delete the company itself from the database
        this.companyRepo.deleteById(companyId);

        return new ResponseEntity<>("Company and users deleted successfully", HttpStatus.NO_CONTENT);

    }


    @GetMapping("api/company/{companyId}/users")
    public ResponseEntity<String> viewUsersInCompany(@PathVariable String companyId) throws JsonProcessingException {
        Optional<Company> company = this.companyRepo.findById(companyId);

        if (company.isEmpty()) {
            throw new NoCompanyException("There is no company with the given Id");
        }

        List<AppUser> companyUsers = this.userRepo.findByCompany(company.get());

        return new ResponseEntity<>(this.objectMapper().writeValueAsString(companyUsers), HttpStatus.OK);
    }

    @GetMapping("api/company/{companyId}/details")
    public ResponseEntity<String> viewCompanyDetails(@PathVariable String companyId) throws JsonProcessingException {
        Optional<Company> company = this.companyRepo.findById(companyId);

        if (company.isEmpty()) {
            throw new NoCompanyException("There is no company with the given Id");
        }

        return new ResponseEntity<>(this.objectMapper().writeValueAsString(company.get()), HttpStatus.OK);
    }
}
