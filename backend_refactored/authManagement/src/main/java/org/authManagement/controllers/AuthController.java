package org.authManagement.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiConfigurations.EmailService;
import org.authManagement.exceptions.CompanyAndUserExceptions;
import org.authManagement.exceptions.CompanyExceptions;
import org.authManagement.exceptions.TokenAndUserExceptions;
import org.authManagement.exceptions.UserExceptions;
import org.authManagement.repositories.CounterRepository;
import org.authManagement.requests.CompanyRegisterRequest;
import org.authManagement.requests.CompanyVerifyRequest;
import org.authManagement.requests.UserRegisterRequest;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import java.util.List;
import java.util.Optional;

@RestController
@Validated
public class AuthController {
    private static final int ROLE_TOKEN_LENGTH = 64;

    private final CustomGenerator generator;
    private final CounterRepository counterRepo;
    private final CompanyRepository companyRepo;
    private final TokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final EmailService emailService;
    private final TokenUserLinkRepository tokenUserLinkRepo;
            
    @Autowired
    public AuthController(
            CompanyRepository companyRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            UserRepository userRepo,
            CounterRepository counterRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            CustomGenerator generator, 
            EmailService emailService
    ) {
        this.companyRepo = companyRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.counterRepo = counterRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.generator = generator;
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

    ////////////////////////////////////// METHODS FOR REGISTERING A COMPANY //////////////////////////////////////
 
    private void validateNewCompany(CompanyRegisterRequest req) {
        // any new company must satisfy the uniqueness constraints:
        // unique id and unique site
        if (this.companyRepo.existsById(req.id())) {
            throw new CompanyExceptions.ExistingCompanyException("There is already a company with the given id.");
        }

        if (this.topLevelDomainRepo.findByDomain(req.topLevelDomain()).isPresent()) {
            throw new CompanyExceptions.ExistingTopLevelDomainException("There is already a company with the given top level domain");
        }

        // a user can belong to only one company: make sure the "ownerEmail" is not already in the database
        if (this.userRepo.existsById(req.ownerEmail())) {
            throw new CompanyAndUserExceptions.MultipleOwnersException("There is already a user with the given email.");
        }

        // make sure the user email matches the main domain
        if (req.mailDomain() != null && !req.ownerEmail().endsWith(req.mailDomain())) {
            throw new CompanyAndUserExceptions.UserCompanyMisalignedException("The email does not match the company domain");
        }
    }

    private void validateOwnerToken(Company company) {
        List<AppToken> companyTokens = this.tokenRepo.findByCompany(company);

        if (! companyTokens.isEmpty()) {
            throw new CompanyAndUserExceptions.MultipleOwnersException("Multiple tokens found for company verification");
        }            
    }

    private void sendCompanyVerificationEmail(String ownerEmail, String ownerToken) {
        String subject = "Company Verification";
        String body = "Here is your company verification token:\n" + ownerToken + "\nThe token expires in 1 hour.";
        this.emailService.sendEmail(ownerEmail, subject, body);  
    }

    @PostMapping("api/auth/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
        // registering a company is done through the following steps: 
        // 1. check the uniqueness constraints
        this.validateNewCompany(req);

        // 2. get the subscription
        Subscription subscription = SubscriptionManager.getSubscription(req.subscription());

        // 3. create the company
        Company company = new Company(req.id(), subscription, req.ownerEmail(), req.mailDomain());

        this.companyRepo.save(company);

        // 4. validate the owner token
        this.validateOwnerToken(company);

        // 5. create the top level domain
        long topLevelDomainId = this.counterRepo.getCount(TopLevelDomain.TOP_LEVEL_DOMAIN_CLASS_NAME);
        TopLevelDomain topLevelDomain = new TopLevelDomain(this.generator.generateId(topLevelDomainId), req.topLevelDomain(), this.encoder().encode(req.topLevelDomain()), company);
        this.topLevelDomainRepo.save(topLevelDomain);

        // 6. create the owner role token
        String ownerTokenId = this.generator.generateId(this.counterRepo.getCount(AppToken.TOKEN_CLASS_NAME));

        // Todo: use a safer token generation mechanism
        String ownerTokenString = this.generator.randomString(ROLE_TOKEN_LENGTH);

        // 7. send the owner token to the owner via email
        if (this.emailService != null) {
            this.sendCompanyVerificationEmail(req.ownerEmail(), ownerTokenString);
        }

        // 8. create the owner token
        AppToken ownerToken = new AppToken(ownerTokenId, 
                                this.encoder().encode(ownerTokenString), 
                                company, 
                                RoleManager.getRole(RoleManager.OWNER_ROLE)
                                );
        
        this.tokenRepo.save(ownerToken);

        // 9. serialize the company
        String companySerialized = this.objectMapper().writeValueAsString(company);
        
        return new ResponseEntity<>(companySerialized,
                HttpStatus.CREATED);
    }


    ////////////////////////////////////// METHODS FOR VERIFYING A COMPANY //////////////////////////////////////

    private Company validateCompanyVerificationRequest(CompanyVerifyRequest req) {
        
        // 1. Check if the company exists
        Optional<Company> c =  this.companyRepo.findById(req.companyId());
        
        if (c.isEmpty()) {
            throw new CompanyExceptions.NoCompanyException("Company with ID " + req.companyId() + " not found");
        }
        
        Company company = c.get();
        
        // 2. check if the email matches the owner email: which means the company was registered with this email
        if (!req.email().equals(company.getOwnerEmail())) {
            throw new CompanyAndUserExceptions.UserCompanyMisalignedException("The provided email does not match the company owner email");
        }

        // 3. Check if the company is already verified
        if (company.getVerified()) {
            throw new CompanyExceptions.CompanyAlreadyVerifiedException("Company is already verified");
        }
        
        // 4. there should be a user registered with the owner email
        if (!this.userRepo.existsById(req.email())) {
            throw new CompanyAndUserExceptions.UserBeforeOwnerException("User with email " + req.email() + " not found");
        }
        
        return company;
    }
    

    private AppToken validateOwnerToken(CompanyVerifyRequest req, Company company) {
        // Find tokens for this company with owner role
        Role ownerRole = RoleManager.getRole(RoleManager.OWNER_ROLE);
        List<AppToken> companyTokens = this.tokenRepo.findByCompanyAndRole(company, ownerRole);

        if (companyTokens.isEmpty()) {
            throw new TokenAndUserExceptions.MissingTokenException("No token found for company verification");
        }

        if (companyTokens.size() > 1) {
            throw new CompanyAndUserExceptions.MultipleOwnersException("Multiple tokens found for company verification");
        }
        
        // Find a matching token
        AppToken ownerToken = null;
        for (AppToken token : companyTokens) {
            if (this.encoder().matches(req.token(), token.getTokenHash())) {
                ownerToken = token;
                break;
            }
        }
        
        if (ownerToken == null) {
            throw new TokenAndUserExceptions.InvalidTokenException("Invalid verification token");
        }
        
        // Check expiration

        if (ownerToken.getTokenState() == AppToken.TokenState.EXPIRED) {
            throw new TokenAndUserExceptions.TokenExpiredException("Verification token has expired");
        }

        if (ownerToken.getTokenState() == AppToken.TokenState.ACTIVE) {
            throw new TokenAndUserExceptions.TokenAlreadyUsedException("The token is already used by another user");
        }
        
        return ownerToken;
    }
    

    @PostMapping("api/auth/register/company/verify")
    public ResponseEntity<String> verifyCompany(@Valid @RequestBody CompanyVerifyRequest req) throws JsonProcessingException {
        // 1. Validate the verification request
        Company company = this.validateCompanyVerificationRequest(req);
        
        // 2. Verify token match and status
        AppToken ownerToken = this.validateOwnerToken(req, company);
        
        // 3. Verify the company
        company.verify();
        this.companyRepo.save(company);
        
        // 4. Mark the token as active
        ownerToken.activate();
        this.tokenRepo.save(ownerToken);
        
        // 5. create a link between the owner and the token
        AppUser ownerUser = this.userRepo.findById(req.email()).get();

        // Generate a random ID for the token-user link
        String tokenUserLinkId = this.generator.generateId(this.counterRepo.getCount(TokenUserLink.TOKEN_USER_LINK_CLASS_NAME));
        TokenUserLink tokenUserLink = new TokenUserLink(tokenUserLinkId, ownerToken, ownerUser);
        this.tokenUserLinkRepo.save(tokenUserLink);

        // 6. Return the serialized company
        return ResponseEntity.ok(this.objectMapper().writeValueAsString(company));
    }


    ////////////////////////////////////// METHODS FOR REGISTERING A USER //////////////////////////////////////

    private Role initialVerification(UserRegisterRequest req) {
        // check if the company exists
        if (!this.companyRepo.existsById(req.companyId())) {
            throw new UserExceptions.UserWithNoCompanyException("No registered company with the given id: " + req.companyId());
        }
        
        // check if the username already exists
        if (this.userRepo.existsById(req.email())) {
            throw new UserExceptions.AlreadyExistingUserException("The username already exists");
        }

        // check the role
        String claimedRoleStr = req.role().toLowerCase();
        // RoleManager.getRole throws an exception if the role does not exist
        Role claimedRole = RoleManager.getRole(claimedRoleStr);

        if (claimedRole != RoleManager.getRole(RoleManager.OWNER_ROLE) && req.roleToken() == null) {
            throw new TokenAndUserExceptions.MissingTokenException("The role token is missing");
        }

        return claimedRole;
    }

    private AppUser registerOwner(UserRegisterRequest req) {
        // at this point, we know the company exists and the username does not exist
        // we need to check if the email matches the owner email
        if (!req.email().equals(this.companyRepo.findById(req.companyId()).get().getOwnerEmail())) {
            throw new CompanyAndUserExceptions.UserCompanyMisalignedException("The passed email does not match the saved owner email");
        }

        // make sure the company is not verified
        Company company = this.companyRepo.findById(req.companyId()).get();
        
        if (company.getVerified()) {
            throw new CompanyAndUserExceptions.MultipleOwnersException("The company is already verified");
        }

        // create the owner user
        AppUser owner = new AppUser(req.email(),
                                req.username(),
                                encoder().encode(req.password()),
                                company, 
                                RoleManager.getRole(req.role()));

        this.userRepo.save(owner);

        return owner;
    }

    private AppToken verifyToken(UserRegisterRequest req, Company company) {
        // Get the role from the request
        Role requestedRole = RoleManager.getRole(req.role());
        
        // Find tokens for this company and role
        List<AppToken> tokens = this.tokenRepo.findByCompanyAndRole(company, requestedRole);

        if (tokens.isEmpty()) {
            throw new TokenAndUserExceptions.TokenNotFoundForRoleException("No token found for role " + req.role() + " in this company");
        }

        // iterate through the tokens and check if the roleToken is correct
        AppToken matchingToken = null;
        
        for (AppToken token : tokens) {
            if (encoder().matches(req.roleToken(), token.getTokenHash())) {
                matchingToken = token; 
                break;
            }
        }

        if (matchingToken == null) {
            throw new TokenAndUserExceptions.InvalidTokenException("The passed token does not match any token for this specific role and company");
        }

        if (matchingToken.getTokenState() == AppToken.TokenState.EXPIRED) {
            throw new TokenAndUserExceptions.TokenExpiredException("The token is expired");
        }

        if (matchingToken.getTokenState() == AppToken.TokenState.ACTIVE) {
            throw new TokenAndUserExceptions.TokenAlreadyUsedException("The token is already used by another user");
        }

        // make sure the token is not linked to another user
        if (this.tokenUserLinkRepo.existsById(matchingToken.getTokenId())) {
            throw new TokenAndUserExceptions.TokenAlreadyUsedException("The token is already used by another user");
        }

        return matchingToken;
    }


    private AppUser registerNonOwner(UserRegisterRequest req) {
        // at this point, we know the company exists and the username does not exist 
        // the roleToken is not null, it needs to be verified
        
        // 1. make sure the company is verified
        Company company = this.companyRepo.findById(req.companyId()).get(); 

        if (!company.getVerified()) {
            throw new CompanyAndUserExceptions.UserBeforeOwnerException("The owner of the company has not verified the company yet");
        }

        // 2. make sure the email matches the domain of the company if any
        String emailDomain = req.email().substring(req.email().indexOf('@') + 1);

        if (!emailDomain.equals(company.getEmailDomain())) {
            throw new CompanyAndUserExceptions.UserCompanyMisalignedException("The use email domain does not match the company domain");
        }

        // 5. create the user
        AppUser user = new AppUser(req.email(),
                                req.username(),
                                encoder().encode(req.password()),
                                company, 
                                RoleManager.getRole(req.role()));

        this.userRepo.save(user);

        // link the user to the token
        AppToken matchingToken = verifyToken(req, company);
        // activate the token
        matchingToken.activate();
        this.tokenRepo.save(matchingToken);

        // Generate a random ID for the token-user link
        String tokenUserLinkId = this.generator.generateId(this.counterRepo.getCount(TokenUserLink.TOKEN_USER_LINK_CLASS_NAME));
        TokenUserLink tokenUserLink = new TokenUserLink(tokenUserLinkId, matchingToken, user);
        this.tokenUserLinkRepo.save(tokenUserLink);

        return user;
    }


    @PostMapping("api/auth/register/user")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterRequest req) throws JsonProcessingException {
        Role role = initialVerification(req); 

        AppUser newUser;

        if (role == RoleManager.getRole(RoleManager.OWNER_ROLE)) {
            newUser = registerOwner(req);
        } else {
            newUser = registerNonOwner(req);
        }

        return ResponseEntity.ok(objectMapper().writeValueAsString(newUser));
    }



}

