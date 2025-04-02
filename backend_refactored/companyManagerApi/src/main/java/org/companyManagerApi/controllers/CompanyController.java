package org.companyManagerApi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.TokenAuthController;
import org.company.entities.Company;
import org.company.repositories.CompanyRepository;
import org.companyManagerApi.exceptions.CompanyMngExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.springframework.validation.annotation.Validated;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.text.SimpleDateFormat;

@RestController
@Validated
public class CompanyController extends TokenAuthController {

    private final CompanyRepository companyRepo;
    private final TokenRepository tokenRepo;

    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public CompanyController(
                        CompanyRepository companyRepo,
                        UserRepository userRepository,
                        TokenUserLinkRepository tokenUserLinkRepository,
                        TokenRepository tokenRepository) {

        super(userRepository, tokenUserLinkRepository);
        this.tokenRepo = tokenRepository;
        this.companyRepo = companyRepo;

        // set the object mapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy hh:mm"));

        this.encoder = new BCryptPasswordEncoder();
    }

    @GetMapping("/api/company/view")
    public ResponseEntity<String> viewCompany(@AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user.getCompany()));
    }

    @GetMapping("api/company/subscription/view")
    public ResponseEntity<String> viewSubscription(@AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user.getCompany().getSubscription()));
    }

    @Transactional
    private void updateSubscriptionTransaction(Company userCompany, Subscription newSub) {
        userCompany.setSubscription(newSub);
        this.companyRepo.save(userCompany);
    }


    @GetMapping("api/company/subscription/update")
    public ResponseEntity<String> updateSubscription(
        @RequestParam("subscription") String subscription,
        @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        
        AppUser user = super.authorizeUserToken(userDetails);
        Company userCompany = user.getCompany();         


        Subscription newSub = SubscriptionManager.getSubscription(subscription);

        if (newSub.equals(userCompany.getSubscription())) {
            throw new CompanyMngExceptions.SameSubscriptionInUpdateException();
        }

        this.updateSubscriptionTransaction(userCompany, newSub);
        
        return ResponseEntity.ok(objectMapper.writeValueAsString(userCompany));
    }
}