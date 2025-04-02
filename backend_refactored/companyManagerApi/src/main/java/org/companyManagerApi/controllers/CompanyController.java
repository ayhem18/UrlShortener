package org.companyManagerApi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.access.Role;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.CommonExceptions;
import org.apiUtils.commonClasses.TokenAuthController;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.CompanyUrlDataRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.companyManagerApi.exceptions.CompanyMngExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.UUID;

@RestController
@Validated
public class CompanyController extends TokenAuthController {

    private final CompanyRepository companyRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final TokenRepository tokenRepo;
    private final CompanyUrlDataRepository companyUrlDataRepo;
    private final ObjectMapper objectMapper;


    @Autowired
    public CompanyController(
                        CompanyRepository companyRepo,
                        TopLevelDomainRepository topLevelDomainRepo,
                        UserRepository userRepository,
                        TokenUserLinkRepository tokenUserLinkRepository,
                        TokenRepository tokenRepository,
                        CompanyUrlDataRepository companyUrlDataRepo) {

        super(userRepository, tokenUserLinkRepository);
        this.companyRepo = companyRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.tokenRepo = tokenRepository;
        this.companyUrlDataRepo = companyUrlDataRepo;
        // set the object mapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy hh:mm"));
    }


    /////////////////////////////////////// view the company details ///////////////////////////////////////
    @GetMapping("/api/company/view")
    public ResponseEntity<String> viewCompany(@AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user.getCompany()));
    }

    /////////////////////////////////////// company Subscription ///////////////////////////////////////

    @Transactional
    private void updateSubscriptionTransaction(Company userCompany, Subscription newSub) {
        userCompany.setSubscription(newSub);
        this.companyRepo.save(userCompany);
    }


    @GetMapping("api/company/subscription/update")
    public ResponseEntity<String> updateSubscription( @RequestParam("subscription") String subscription, @AuthenticationPrincipal UserDetails userDetails) 
        throws JsonProcessingException {
        
        AppUser user = super.authorizeUserToken(userDetails);
        Company userCompany = user.getCompany();         


        Subscription newSub = SubscriptionManager.getSubscription(subscription);

        if (newSub.equals(userCompany.getSubscription())) {
            throw new CompanyMngExceptions.SameSubscriptionInUpdateException();
        }

        this.updateSubscriptionTransaction(userCompany, newSub);
        
        return ResponseEntity.ok(objectMapper.writeValueAsString(userCompany));
    }

    @GetMapping("api/company/subscription/view")
    public ResponseEntity<String> viewSubscription(@AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
        return ResponseEntity.ok(objectMapper.writeValueAsString(user.getCompany().getSubscription()));
    }

    /////////////////////////////////////// update the company domain ///////////////////////////////////////
    @Transactional
    private void updateDomainTransaction(Company userCompany, 
                                        TopLevelDomain currentCompanyDomain, 
                                        String newDomain, 
                                        boolean deprecate) {
        if (deprecate) {
            currentCompanyDomain.deprecate();
        } else {
            currentCompanyDomain.deactivate();
        }

        // find an id for the new top level domain
        String newDomainId = UUID.randomUUID().toString();

        // make sure the new domain id is unique
        while (this.topLevelDomainRepo.existsById(newDomainId)) {
            newDomainId = UUID.randomUUID().toString();
        }

        // create a new top level domain with the new domain
        TopLevelDomain newCompanyDomain = new TopLevelDomain(newDomainId, newDomain, userCompany);
        this.topLevelDomainRepo.save(newCompanyDomain); 
    }
    
    @GetMapping("api/company/domain/update")
    public ResponseEntity<String> updateDomain(@RequestParam("newDomain") String newDomain, 
                                                @RequestParam(value="deprecate") boolean deprecate,                                            
                                                @AuthenticationPrincipal UserDetails userDetails)
                                                throws JsonProcessingException {
        // authorize the user
        AppUser user = super.authorizeUserToken(userDetails);
        Company userCompany = user.getCompany();                                        
        
        // get the current active domain
        TopLevelDomain currentCompanyDomain = this.topLevelDomainRepo.
                                            findFirstByCompanyAndDomainState(userCompany, TopLevelDomain.DomainState.ACTIVE).get();
        
        // set the state of the old domain and update the  
        this.updateDomainTransaction(userCompany, currentCompanyDomain, newDomain, deprecate);
        return ResponseEntity.ok(objectMapper.writeValueAsString(userCompany));
    }


    /////////////////////////////////////// view the company users ///////////////////////////////////////
    private void validateRoleAuthority(AppUser currentUser, Role lowerRole, String errorMessage) {
        // Check if current user's role has higher priority than the requested role
        if (!currentUser.getRole().isHigherPriorityThan(lowerRole)) {
            throw new CommonExceptions.InsufficientRoleAuthority(
                errorMessage);
        }
    }

    @GetMapping("api/company/users/view")
    public ResponseEntity<String> viewUser(@RequestParam(required=false, value="userEmail") String userEmail, 
                                            @AuthenticationPrincipal UserDetails userDetails) 
                                            throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
                                            
        if (userEmail == null) {
            // if no user email is provided, return the current user
            return ResponseEntity.ok(objectMapper.writeValueAsString(user));
        }

        AppUser targetUser = this.userRepo.findById(userEmail)
                .orElseThrow(() -> new CommonExceptions.UserNotFoundException(userEmail)); 

        // make sure the current user has the authority to view the target user)
        this.validateRoleAuthority(user, targetUser.getRole(), "Cannot view user with equal or higher priority");

        // at this point, the target user exists, the current user has the authoriy to view the target user
        return ResponseEntity.ok(objectMapper.writeValueAsString(targetUser));
    }


    @GetMapping("api/company/delete")
    public ResponseEntity<String> deleteCompany(@AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        AppUser user = super.authorizeUserToken(userDetails);
        
        // deleting a company, means deleting all the tokens-user links, tokens, urlCompanyData, topLevelDomain, and users

        // delete all tokens-user links
        this.tokenUserLinkRepo.deleteByCompany(user.getCompany());

        // delete all tokens
        this.tokenRepo.deleteByCompany(user.getCompany());
        
        // delete all top level domains
        this.topLevelDomainRepo.deleteByCompany(user.getCompany());

        // delete companyUrlData
        this.companyUrlDataRepo.deleteByCompany(user.getCompany());

        // delete all users in the company
        this.userRepo.deleteByCompany(user.getCompany());

        // delete the company
        this.companyRepo.delete(user.getCompany());
        return ResponseEntity.ok(objectMapper.writeValueAsString(user.getCompany()));
    }
}