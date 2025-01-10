package com.url_shortener.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.url_shortener.CustomErrorMessage;
import com.url_shortener.CustomRandomGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.tokens.FlowEntryToken;

import java.util.HashMap;
import java.util.HashSet;


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


    @PostMapping("")
    public ResponseEntity<String> RegisterCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
        if (this.companyRepo.findById(req.id()).isPresent()) {
            throw new ExistingCompanyException("There is already a registered company with the given email.");
        }

        // create the role tokens
        HashMap<String, String> roleTokens = new HashMap<>();

        // add the owner role token
        roleTokens.put(Owner.role(), String.valueOf(this.generator.randomString(ROLE_TOKEN_LENGTH).hashCode()));

        // add the admin role token
        roleTokens.put(Admin.role(), String.valueOf(this.generator.randomString(ROLE_TOKEN_LENGTH).hashCode()));

        // add the registeredUser role token
        roleTokens.put(RegisteredUser.role(), String.valueOf(this.generator.randomString(ROLE_TOKEN_LENGTH).hashCode()));

        // build the company object

        Company newCompany = new Company(req.id(), req.site(), roleTokens);

        // save it to the database
        this.companyRepo.save(newCompany);

        return new ResponseEntity<>(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(newCompany),
                HttpStatus.CREATED);
    }
}
