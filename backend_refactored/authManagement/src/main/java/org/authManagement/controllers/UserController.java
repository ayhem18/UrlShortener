package org.authManagement.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.authManagement.exceptions.CompanyAndUserExceptions;
import org.authManagement.exceptions.TokenAndUserExceptions;
import org.authManagement.exceptions.UserExceptions;
import org.authManagement.repositories.CounterRepository;
import org.authManagement.requests.UserRegisterRequest;

import java.util.List;

import org.access.Role;
import org.access.RoleManager;
import org.company.entities.Company;
import org.company.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;
import org.tokens.repositories.TokenUserLinkRepository;

@RestController
@Validated
public class UserController {
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    private final CustomGenerator generator;
    private final CounterRepository counterRepo;

    @Autowired
    public UserController(CompanyRepository companyRepo,
                          UserRepository userRepo,
                          TokenRepository tokenRepo,
                          TokenUserLinkRepository tokenUserLinkRepo,
                          CustomGenerator generator,
                          CounterRepository counterRepo) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.generator = generator;
        this.counterRepo = counterRepo;
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



}
