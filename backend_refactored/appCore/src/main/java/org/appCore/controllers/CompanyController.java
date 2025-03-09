package org.appCore.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.appCore.exceptions.CompanyAndUserExceptions;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.entities.Token;
import org.tokens.repositories.TokenRepository;
import org.access.Subscription;
import org.access.RoleManager;
import org.access.SubscriptionManager;
import org.appCore.configurations.EmailService;
import org.appCore.entities.CollectionCounter;
import org.appCore.repositories.CounterRepository;
import org.appCore.requests.CompanyRegisterRequest;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;


@RestController
@Validated
public class CompanyController {
    private static final int ROLE_TOKEN_LENGTH = 64;

    private final CustomGenerator generator;
    private final CounterRepository counterRepo;
    private final CompanyRepository companyRepo;
    private final TokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final EmailService emailService;
        
    @Autowired
    public CompanyController(
            CompanyRepository companyRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            UserRepository userRepo,
            CounterRepository counterRepo,
            TokenRepository tokenRepo,
            CustomGenerator generator, 
            EmailService emailService
    ) {
        this.companyRepo = companyRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.generator = generator;
        this.counterRepo = counterRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
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


    private void validateNewCompany(CompanyRegisterRequest req) {
        // any new company must satisfy the uniqueness constraints:
        // unique id and unique site
        if (this.companyRepo.existsById(req.id())) {
            throw new CompanyAndUserExceptions.ExistingCompanyException("There is already a company with the given id.");
        }

        if (this.topLevelDomainRepo.findByDomain(req.topLevelDomain()).isPresent()) {
            throw new CompanyAndUserExceptions.ExistingSiteException("There is already a company with the given top level domain");
        }

        if (this.userRepo.existsById(req.ownerEmail())) {
            throw new CompanyAndUserExceptions.MultipleOwnersException("There is already a user with the given email.");
        }

        // make sure the user email matches the main domain
        if (req.mailDomain() != null && !req.ownerEmail().endsWith(req.mailDomain())) {
            throw new CompanyAndUserExceptions.CompanyUserMailMisalignmentException("The email does not match the main domain");
        }
    }

    private void sendCompanyVerificationEmail(String ownerEmail, String ownerToken) {
        // send an email to the owner
        String subject = "Company Verification";
        String body = "Here is your company verification token: " + ownerToken + "\nThe token expires in 1 hour.";
        this.emailService.sendEmail(ownerEmail, subject, body);  
    }


    @PostMapping("api/auth/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
        // registering a company is done through the following steps: 
        // 1. check the uniqueness constraints x
        validateNewCompany(req);

        // 2. hash the domain
        String hashedDomain = this.encoder().encode(req.topLevelDomain()); 

        // 3. get the subscription
        Subscription subscription = SubscriptionManager.getSubscription(req.subscription());

        // 4. create the company
        Company company = new Company(req.id(), subscription, req.mailDomain(), req.ownerEmail());

        this.companyRepo.save(company);

        // 5. create the top level domain
        long topLevelDomainId = this.counterRepo.getCount(TopLevelDomain.TOP_LEVEL_DOMAIN_CLASS_NAME);
        TopLevelDomain topLevelDomain = new TopLevelDomain(this.generator.generateId(topLevelDomainId), req.topLevelDomain(), hashedDomain, company);

        // 6. save the top level domain
        this.topLevelDomainRepo.save(topLevelDomain);

        // 6. create the owner role token
        String ownerTokenId = this.generator.generateId(this.counterRepo.getCount(Token.TOKEN_CLASS_NAME));

        // Todo: use safer token generation mechanism
        String ownerTokenString = this.generator.randomString(ROLE_TOKEN_LENGTH);

        // 7. send the owner token to the owner via email
        this.sendCompanyVerificationEmail(req.ownerEmail(), ownerTokenString);

        // 8. hash the owner token
        String ownerTokenHash = this.encoder().encode(ownerTokenString);

        // 8. create the owner token
        Token ownerToken = new Token(ownerTokenId, ownerTokenHash);
        // save the owner token
        this.tokenRepo.save(ownerToken);

        // both the company and the owner token are saved, they both need to be serialized

        String companySerialized = this.objectMapper().writeValueAsString(company);
        
        return new ResponseEntity<>(companySerialized,
                HttpStatus.CREATED);
    }


//    @PostMapping("api/auth/register/company")
//    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
//        // check the uniqueness constraints
//        validateNewCompany(req);
//        // the new company is valid: get its creation order
//        long companyOrder = this.getCompanyCount();
//
//        // create the role tokens
//        HashMap<String, String> roleTokens = new HashMap<>();
//
//        // add the owner role token
//        roleTokens.put(RoleManager.OWNER_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));
//
//        // add the admin role token
//        roleTokens.put(RoleManager.ADMIN_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));
//
//        // add the registeredUser role token
//        roleTokens.put(RoleManager.EMPLOYEE_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));
//
//        // get the subscription from the SubscriptionManager
//        Subscription sub = SubscriptionManager.getSubscription(req.subscription());
//
//        // build the company object
//        CompanyWrapper wrapper = new CompanyWrapper(req.id(), req.site(), sub, roleTokens, this.encoder(), this.generator, companyOrder);
//        // make sure to call the serialize first, so that the "serializeSensitiveCount" field will be saved as "4"
//        // in the database preventing the serialization of sensitive information beyond the very first time
//        String companySerialized = wrapper.serialize(this.objectMapper());
//
//        wrapper.save(this.companyRepo);
//
//        return new ResponseEntity<>(companySerialized,
//                HttpStatus.CREATED);
//    }

    // @DeleteMapping("api/company/{companyId}")
    // public ResponseEntity<String> deleteCompany(@PathVariable String companyId,
    //                                             @AuthenticationPrincipal UserDetails currentUserDetails) throws RuntimeException{

    //     Optional<Company> company = this.companyRepo.findById(companyId);

    //     if (company.isEmpty()) {
    //         throw new CompanyExceptions.NoCompanyException("There is no company with the given Id");
    //     }

    //     // this function can be called by the Owner user of the company
    //     AppUser currentUser = this.userRepo.findById(currentUserDetails.getUsername()).get();


    //     if (!currentUser.getRole().toString().equals(RoleManager.OWNER_ROLE)) {
    //         throw new RuntimeException("Man you messed up the authentication");
    //     }

    //     // delete all users in the given company
    //     this.userRepo.deleteByCompany(company.get());

    //     // delete the company itself from the database
    //     this.companyRepo.deleteById(companyId);

    //     return new ResponseEntity<>("Company and users deleted successfully", HttpStatus.NO_CONTENT);
    // }


//    @GetMapping("api/company/{companyId}/users")
//    public ResponseEntity<String> viewUsersInCompany(@PathVariable String companyId) throws JsonProcessingException {
//        Optional<Company> company = this.companyRepo.findById(companyId);
//
//        if (company.isEmpty()) {
//            throw new NoCompanyException("There is no company with the given Id");
//        }
//
//        List<AppUser> companyUsers = this.userRepo.findByCompany(company.get());
//
//        return new ResponseEntity<>(this.objectMapper().writeValueAsString(companyUsers), HttpStatus.OK);
//    }

    // @GetMapping("api/company/{companyId}/details")
    // public ResponseEntity<String> viewCompanyDetails(@PathVariable String companyId) throws JsonProcessingException {
    //     Optional<Company> company = this.companyRepo.findById(companyId);

    //     if (company.isEmpty()) {
    //         throw new CompanyExceptions.NoCompanyException("There is no company with the given Id");
    //     }

    //     return new ResponseEntity<>(this.objectMapper().writeValueAsString(company.get()), HttpStatus.OK);
    // }
}
