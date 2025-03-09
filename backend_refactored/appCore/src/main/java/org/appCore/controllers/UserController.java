package org.appCore.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.appCore.exceptions.CompanyAndUserExceptions;
import org.appCore.exceptions.TokenAndUserExceptions;
import org.appCore.exceptions.UserExceptions;
import org.appCore.requests.UserRegisterRequest;

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
import org.tokens.repositories.TokenUserLinkRepository;

@RestController
@Validated
public class UserController {
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;

    @Autowired
    public UserController(CompanyRepository companyRepo,
                          UserRepository userRepo,
                          TokenRepository tokenRepo,
                          TokenUserLinkRepository tokenUserLinkRepo) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
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


    private Role initialVerification(UserRegisterRequest req) {
        // check if the company exists
        if (!this.companyRepo.existsById(req.companyId())) {
            throw new UserExceptions.UserWithNoCompanyException("No registered company with the given id: " + req.companyId());
        }
        
        // check if the username already exists
        if (this.userRepo.existsById(req.username())) {
            throw new UserExceptions.AlreadyExistingUserException("The username already exists");
        }

        // check the role
        String claimedRoleStr = req.role().toLowerCase();
        // RoleManager.getRole throws an exception if the role does not exist
        Role claimedRole = RoleManager.getRole(claimedRoleStr);

        if (claimedRole != RoleManager.getRole(RoleManager.OWNER_ROLE) && req.roleToken() == null) {
            throw new UserExceptions.MissingTokenException("The role token is missing");
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
        // set the matching token to active
        matchingToken.activate();
        this.tokenRepo.save(matchingToken);

        TokenUserLink tokenUserLink = new TokenUserLink(matchingToken, user);
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
