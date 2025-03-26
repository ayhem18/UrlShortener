package org.authManagement.tests;

import org.authManagement.controllers.AuthController;
import org.authManagement.exceptions.CompanyAndUserExceptions;
import org.authManagement.exceptions.CompanyExceptions;
import org.authManagement.exceptions.TokenAndUserExceptions;
import org.authManagement.exceptions.UserExceptions;
import org.authManagement.requests.CompanyRegisterRequest;
import org.authManagement.requests.CompanyVerifyRequest;
import org.authManagement.requests.UserRegisterRequest;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.stubs.repositories.*;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;
import org.utils.CustomGenerator;


import static org.junit.jupiter.api.Assertions.*;

import org.access.Role;
import org.access.RoleManager;
import org.access.SubscriptionManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthManagementControllerTest {

    private final StubCompanyRepo companyRepo;
    private final StubUserRepo userRepo;
    private final StubTopLevelDomainRepo topLevelDomainRepo;

    private final StubTokenRepo tokenRepo;
    private final StubTokenUserLinkRepo tokenUserLinkRepo;

    private final StubCompanyUrlDataRepo companyUrlDataRepo;
    private final StubCounterRepo counterRepo;

    private final CustomGenerator gen = new CustomGenerator();
    private final AuthController authCon;

    // @BeforeEach
    public void setUp() {
        // Clear repositories to ensure a clean state
        clearRepositories();

        // Initialize default test data for each repository

        // 1. Add default companies
        Company youtube = new Company("aaa", "youtube", "youtubeAddress", "owner@youtube.com", "youtube.com", SubscriptionManager.getSubscription("TIER_1"));
        Company github = new Company("bbb", "github", "githubAddress", "owner@github.com", "github.com", SubscriptionManager.getSubscription("TIER_1"));
        companyRepo.save(youtube);
        companyRepo.save(github);

        // 2. Add default domains for these companies
        TopLevelDomain youtubeDomain = new TopLevelDomain("domain1", "youtube.com", youtube);
        TopLevelDomain githubDomain = new TopLevelDomain("domain2", "github.com", github);
        topLevelDomainRepo.save(youtubeDomain);
        topLevelDomainRepo.save(githubDomain);

        
        // owners
        AppUser youtubeOwner = new AppUser(
            "owner@youtube.com",                
            "ytowner",                          
            "password123",                      
            "YouTube",                          
            "Owner",                            
            "Test",                             
            youtube,                            
            RoleManager.getRole(RoleManager.OWNER_ROLE)  // Role
        );
        AppUser githubOwner = new AppUser(
            "owner@github.com",                
            "ghowner",                          
            "password123",                      
            "GitHub",                          
            "Owner",                            
            null,                             
            github,                             
            RoleManager.getRole(RoleManager.OWNER_ROLE)  
        );

        // admins

        AppUser youtubeAdmin = new AppUser(
            "admin@youtube.com",                
            "ytadmin",                          
            "password123",                      
            "YouTube",                          
            "Admin",                            
            null,                             
            youtube,                            
            RoleManager.getRole(RoleManager.ADMIN_ROLE)  
        );
        
        AppUser githubAdmin = new AppUser(
            "admin@github.com",                
            "ghadmin",                          
            "password123",                      
            "GitHub",                          
            "Admin",                            
            null,                             
            github,                             
            RoleManager.getRole(RoleManager.ADMIN_ROLE)  
        );

        // employees

        
        AppUser youtubeEmployee = new AppUser(
            "employee@youtube.com",                
            "ytemployee",                          
            "password123",                      
            "YouTube",                          
            "Employee",                            
            null,                             
            youtube,                             
            RoleManager.getRole(RoleManager.EMPLOYEE_ROLE)  
        );

        AppUser githubEmployee = new AppUser(
            "employee@github.com",                
            "ghemployee",                          
            "password123",                      
            "GitHub",                          
            "Employee",                            
            null,                             
            github,                             
            RoleManager.getRole(RoleManager.EMPLOYEE_ROLE)  
        );

        userRepo.saveAll(List.of(youtubeOwner, githubOwner, youtubeAdmin, githubAdmin, youtubeEmployee, githubEmployee));

        CompanyUrlData urlDataYoutube = new CompanyUrlData(this.gen.randomAlphaString(20), youtube, "hash_youtube");
        companyUrlDataRepo.save(urlDataYoutube);

        CompanyUrlData urlDataGithub = new CompanyUrlData(this.gen.randomAlphaString(20), github, "hash_github");
        companyUrlDataRepo.save(urlDataGithub);
    }

    @AfterEach
    public void tearDown() {
        clearRepositories();
    }

    private void clearRepositories() {
        // Clear all repositories to ensure a clean state
        companyRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll(); 
        tokenRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        // The counter repo might need special handling if it doesn't have a clear method
        // We might need to reset it to initial state instead
    }

    public AuthManagementControllerTest() {
        this.companyRepo = new StubCompanyRepo();
        this.topLevelDomainRepo = new StubTopLevelDomainRepo(this.companyRepo);
        this.userRepo = new StubUserRepo(this.companyRepo);
        this.tokenRepo = new StubTokenRepo(this.companyRepo);
        this.tokenUserLinkRepo = new StubTokenUserLinkRepo(this.tokenRepo, this.userRepo);
        this.companyUrlDataRepo = new StubCompanyUrlDataRepo(); 
        this.counterRepo = new StubCounterRepo();
    
        
        // set a stubCustomGenerator, so we can verify the registerCompany method properly
        this.authCon = new AuthController(this.companyRepo,
                this.topLevelDomainRepo,
                this.userRepo,
                this.tokenRepo,
                this.tokenUserLinkRepo,
                this.counterRepo,
                this.companyUrlDataRepo,
                null,
                this.gen);
    }

   // Helper method for password encoding
    private PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    //////////////////////// register a company ////////////////////////
    @Test
    void testUniquenessConstraints() {
        int i = 0;
        // 1. company ID uniqueness test
        for (Company c : this.companyRepo.findAll()) {
            i += 1;
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(c.getId(),
                    "companyName_" + i,
                    "companyAddress" + i,
                    "random_domain.com",
                    "someEmail@gmail.com",
                    null,
            c.getSubscription().getTier());

            // Count repositories before attempting operation
            long companyCountBefore = companyRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long userCountBefore = userRepo.count();
            long domainCountBefore = topLevelDomainRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();

            Assertions.assertThrows(
                    CompanyExceptions.ExistingCompanyException.class,
                    () -> this.authCon.registerCompany(req)
            );
            
            // Verify no objects were saved
            assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
            assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(domainCountBefore, topLevelDomainRepo.count(), "No new domains should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "No new token user links should be saved on failure");
            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
            assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");

        }

        // 2. top level domain uniqueness test
        for (TopLevelDomain domain : this.topLevelDomainRepo.findAll()) {
            i += 1 ; // update the counter to make sure the address and names are uqnieu
            // Generate a unique company ID for this test case
            String uniqueId = "unique_" + this.gen.randomString(8);

            // Create a request with unique ID but existing domain
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    "companyName_" + i,
                    "companyAddress" + i,
                    domain.getDomain(),
                    "owner@" + domain.getDomain(),  // Use matching email for domain
                    domain.getDomain(),
                    "TIER_1"
                    );

            // Count repositories before attempting operation
            long companyCountBefore = companyRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long userCountBefore = userRepo.count();
            long domainCountBefore = topLevelDomainRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();

            // Verify that an ExistingTopLevelDomainException is thrown
            Assertions.assertThrows(
                    CompanyExceptions.ExistingTopLevelDomainException.class,
                    () -> this.authCon.registerCompany(req)
            );
            
            // Verify no objects were saved
            assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
            assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(domainCountBefore, topLevelDomainRepo.count(), "No new domains should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "No new token user links should be saved on failure");
            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
            assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
        }


        // 3. owner email uniqueness test
        for (AppUser user : this.userRepo.findAll()) {
            i += 1;
            // Get the user's email
            String userEmail = user.getEmail();


            // Generate unique domain and ID
            String uniqueId = "unique_" + this.gen.randomString(8);
            String uniqueDomain = "unique" + this.gen.randomAlphaString(5) + ".com";

            // Create request with unique ID, unique domain, but existing email
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    "companyName_" + i,
                    "companyAddress" + i,
                    uniqueDomain,
                    userEmail,  // Existing user email
                    null,
                    "TIER_1"
                    );

            // Count repositories before attempting operation
            long companyCountBefore = companyRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long userCountBefore = userRepo.count();
            long domainCountBefore = topLevelDomainRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();

            // Verify that a MultipleOwnersException is thrown
            assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> this.authCon.registerCompany(req)
            );
            
            // Verify no objects were saved
            assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
            assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(domainCountBefore, topLevelDomainRepo.count(), "No new domains should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "No new token user links should be saved on failure");
            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
            assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
        }

        // 4. company name uniqueness test
        for (Company c : this.companyRepo.findAll()) {
            i += 1;
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    c.getId(),
                    c.getCompanyName(),
                    c.getCompanyAddress(),
                    "unique" + this.gen.randomAlphaString(5) + ".com",
                    "owner@" + "unique" + this.gen.randomAlphaString(5) + ".com",
                    null,
                    "TIER_1"
                    );

            // Count repositories before attempting operation
            long companyCountBefore = companyRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long userCountBefore = userRepo.count();
            long domainCountBefore = topLevelDomainRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();

            Assertions.assertThrows(
                CompanyExceptions.ExistingCompanyException.class,
                () -> this.authCon.registerCompany(req)
            );
            
            // Verify no objects were saved
            assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
            assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(domainCountBefore, topLevelDomainRepo.count(), "No new domains should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "No new token user links should be saved on failure");
            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
            assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
        }
    }

    @Test 
    void testTokenConstraintsRegisterCompany() {
        
        for (String roleString : RoleManager.ROLES_STRING) {
            for (int i = 0; i <= 10; i ++) {

                // 1. Generate a random company id that doesn't exist in the repo
                String randomCompanyId = "test_company_" + gen.randomAlphaString(8);
                while (companyRepo.existsById(randomCompanyId)) {
                    randomCompanyId = "test_company_" + gen.randomAlphaString(8);
                }

                // Create a dummy company to associate with the token
                Company dummyCompany = new Company(
                        randomCompanyId,
                        "Test Company " + randomCompanyId,  // Meaningful company name
                        "123 Test Street, TestCity",        // Physical address
                        "owner@example.com",                // Owner email
                        "example.com",                      // Email domain
                        SubscriptionManager.getSubscription("TIER_1")
                );

                // 2. Create and save a token for this company with the current role
                Role role = RoleManager.getRole(roleString);
                AppToken token = new AppToken(
                        "token_" + randomCompanyId,
                        "hash_" + randomCompanyId,
                        dummyCompany,
                        role
                );
                tokenRepo.save(token);

                // 3. Create a company register request with the same ID
                CompanyRegisterRequest req = new CompanyRegisterRequest(
                        randomCompanyId,
                        "Company " + randomCompanyId,         // Company name
                        "123 Test Avenue",                    // Company address
                        "unique" + gen.randomAlphaString(5) + ".com", // Top level domain
                        "newowner@example.com",               // Owner email
                        "example.com",                        // Mail domain
                        "TIER_1"                              // Subscription
                );

                // Count repositories before attempting operation
                long companyCountBefore = companyRepo.count();
                long tokenCountBefore = tokenRepo.count();
                long userCountBefore = userRepo.count();
                long domainCountBefore = topLevelDomainRepo.count();
                long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
                long companyUrlDataCountBefore = companyUrlDataRepo.count();
                long counterCountBefore = counterRepo.count();

                // Final randomCompanyId for use in lambda
                String finalCompanyId = randomCompanyId;

                // Verify that registering a company with this ID throws an exception
                assertThrows(
                        CompanyAndUserExceptions.MultipleOwnersException.class,
                        () -> authCon.registerCompany(req),
                        "Should fail when token already exists for company ID " + finalCompanyId
                );
                
                // Verify no objects were saved
                assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
                assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
                assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
                assertEquals(domainCountBefore, topLevelDomainRepo.count(), "No new domains should be saved on failure");
                assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "There should be no token user links");  
                assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "There should be no company url data");  
                assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
            }
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessRegisterCompany() {
        // everything is cleared, so no need to worry about clearing the repositories
            
        for (int i = 0; i < 100; i++) {
            // Generate unique domain
            String domain = "unique" + i + gen.randomAlphaString(10) + ".com";
            String ownerEmail = "owner" + i + "@" + "new_mail_domain.com";

            String randomCompanyId = gen.randomAlphaString(20);

            // 2. Create and submit a company registration request
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                randomCompanyId,                      // ID
                "New Test Company " + i,                   // Company name
                "456 Business Lane " + i,                  // Company address
                domain,                               // Top level domain
                ownerEmail,                           // Owner email
                "new_mail_domain.com",                               // Mail domain
                "TIER_1"                              // Subscription
            );
            
            // Register the company and verify no exceptions are thrown
            assertDoesNotThrow(() -> this.authCon.registerCompany(req));
            
            // 3. Verify that a company with this ID now exists
            assertTrue(companyRepo.existsById(randomCompanyId), 
                "Company should exist in repository after registration");
            
            // Get the newly created company
            Company newCompany = companyRepo.findById(randomCompanyId).orElse(null);
            assertNotNull(newCompany, "Company should not be null");
            
            // Verify company properties
            assertEquals(randomCompanyId, newCompany.getId());
            assertEquals("new_mail_domain.com", newCompany.getEmailDomain());
            assertEquals(ownerEmail, newCompany.getOwnerEmail());
            assertFalse(newCompany.getVerified(), "New company should not be verified");
            
            // 4. Verify a token was created for this company
            Role ownerRole = RoleManager.getRole(RoleManager.OWNER_ROLE);
            List<AppToken> tokens = tokenRepo.findByCompanyAndRole(newCompany, ownerRole);
            
            assertEquals(1, tokens.size(), "There should be exactly one owner token");
            
            // Verify token properties
            AppToken ownerToken = tokens.getFirst();
            assertEquals(newCompany.getId(), ownerToken.getCompany().getId());
            assertEquals(RoleManager.getRole(RoleManager.OWNER_ROLE).role(), ownerToken.getRole().role());
            
            // Verify that a TopLevelDomain was created
            List<TopLevelDomain> domains = topLevelDomainRepo.findByCompany(newCompany);
            assertFalse(domains.isEmpty(), "A domain should exist for the company");
            assertEquals(domain, domains.getFirst().getDomain());

            // verify the counterRepo was updated 
            assertEquals(1, counterRepo.findAll().size(), "CounterRepo should have only one entry");
            assertEquals(i + 1, counterRepo.findByCollectionName(Company.COMPANY_COLLECTION_NAME).get().getCount(), "the counter repo must track the count correctly");

            // verify that the companyUrlData was created
            Optional<CompanyUrlData> urlData = companyUrlDataRepo.findFirstByCompany(newCompany);
            assertTrue(urlData.isPresent(), "CompanyUrlData should exist for the company");
            assertEquals(newCompany.getId(), urlData.get().getCompany().getId());
            
            String expectedHash = this.gen.generateId(counterRepo.findByCollectionName(Company.COMPANY_COLLECTION_NAME).get().getCount() - 1 + AuthController.companySiteHashOffset);
            assertEquals(expectedHash, urlData.get().getCompanyDomainHashed());
            
            // verify the counts
            assertEquals(0, tokenUserLinkRepo.count(), "There should be no token user links");  
            assertEquals(0, userRepo.count(), "There should be no users");  
            assertEquals(1, counterRepo.count(), "There should be exactly one counter");  
            assertEquals(i + 1, tokenRepo.count(), "There should be exactly one token");
            assertEquals(i + 1, companyRepo.count(), "There should be exactly one company");  
            assertEquals(i + 1, counterRepo.findByCollectionName(Company.COMPANY_COLLECTION_NAME).get().getCount(), "the counter repo must track the count correctly");
            assertEquals(i + 1, companyUrlDataRepo.count(), "There should be exactly one company url data");
        
        }
    }

    //////////////////////// register a user ////////////////////////

    @Test
    void testRegisterUserInitialConstraints() {

        // add a few companies and users to the repositories
        setUp();

        // 1. Test that registration fails when the username already exists
        // Loop through all existing users to test constraint
        int i = 0;
        for (AppUser existingUser : userRepo.findAll()) {
            i += 1;
            UserRegisterRequest existingUserRequest = new UserRegisterRequest(
                        existingUser.getEmail()   ,          // email
                        "someUsername",                       // username
                        "password123",                        // password
                        "firstName_" + i,
                        "lastName_" + i,
                        "middleName_" + i,
                        existingUser.getCompany().getId(),    // companyId
                        RoleManager.EMPLOYEE_ROLE,            // role
                        "someToken"                           // roleToken
            );

            // Count repositories before attempting operation
            long userCountBefore = userRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long counterCountBefore = counterRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long companyCountBefore = companyRepo.count(); 
            long topLevelDomainCountBefore = topLevelDomainRepo.count();
            long tokenCountBefore = tokenRepo.count();

            assertThrows(
               UserExceptions.AlreadyExistingUserException.class,
               () -> authCon.registerUser(existingUserRequest),
               "Should fail when a user with the email already exists: " + existingUser.getEmail()
            );
            
            // Verify no objects were saved
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");
            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
            assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
            assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
            assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
            assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");
        }

        i += 1;
       // 2. Test that registration fails when the company does not exist
       String nonExistentCompanyId = "non_existent_company_" + gen.randomAlphaString(8);
       UserRegisterRequest nonExistentCompanyRequest = new UserRegisterRequest(
               "new_user@example.com",             
               "newUser",                          
               "password123",                       
               "firstName_" + i,
               "lastName_" + i,
               "middleName_" + i,
               nonExistentCompanyId,                
           RoleManager.EMPLOYEE_ROLE,           
           "someToken"                          
       );

       // Count repositories before attempting operation
       long userCountBefore1 = userRepo.count();
       long tokenUserLinkCountBefore1 = tokenUserLinkRepo.count();
       long counterCountBefore1 = counterRepo.count();
       long companyUrlDataCountBefore1 = companyUrlDataRepo.count();
       long companyCountBefore1 = companyRepo.count();
       long topLevelDomainCountBefore1 = topLevelDomainRepo.count();
       long tokenCountBefore1 = tokenRepo.count();

       assertThrows(
           UserExceptions.UserWithNoCompanyException.class,
           () -> authCon.registerUser(nonExistentCompanyRequest),
           "Should fail when the company ID doesn't exist"
       );
       
       // Verify no objects were saved
       assertEquals(userCountBefore1, userRepo.count(), "No new users should be saved on failure");
       assertEquals(tokenUserLinkCountBefore1, tokenUserLinkRepo.count(), 
                   "No new token-user links should be saved on failure");
       assertEquals(companyUrlDataCountBefore1, companyUrlDataRepo.count(), "No new company url data should be saved on failure");
       assertEquals(counterCountBefore1, counterRepo.count(), "No new counters should be saved on failure");
       assertEquals(companyCountBefore1, companyRepo.count(), "No new companies should be saved on failure");
       assertEquals(topLevelDomainCountBefore1, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
       assertEquals(tokenCountBefore1, tokenRepo.count(), "No new tokens should be saved on failure");

       i += 1;
       // 3. Test that registration fails when the role does not exist
       String invalidRole = "INVALID_ROLE_";
       Company existingCompany = companyRepo.findAll().getFirst();
       UserRegisterRequest invalidRoleRequest = new UserRegisterRequest(
               "new_user@example.com",              
               "newUser",                           
               "password123",                       
               "firstName_" + i,
               "lastName_" + i,
               "middleName_" + i,
               existingCompany.getId(),            
           invalidRole,                         
           "someToken"                          
       );


       // Count repositories before attempting operation
       long userCountBefore2 = userRepo.count();
       long tokenUserLinkCountBefore2 = tokenUserLinkRepo.count();
       long counterCountBefore2 = counterRepo.count();
       long companyUrlDataCountBefore2 = companyUrlDataRepo.count();
       long companyCountBefore2 = companyRepo.count();
       long topLevelDomainCountBefore2 = topLevelDomainRepo.count();
       long tokenCountBefore2 = tokenRepo.count();


       assertThrows(
           RoleManager.NoExistingRoleException.class,
           () -> authCon.registerUser(invalidRoleRequest),
           "Should fail when the role doesn't exist"
       );
       
       // Verify no objects were saved
       assertEquals(userCountBefore2, userRepo.count(), "No new users should be saved on failure");
       assertEquals(tokenUserLinkCountBefore2, tokenUserLinkRepo.count(),
                   "No new token-user links should be saved on failure");
       assertEquals(companyUrlDataCountBefore2, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
       assertEquals(counterCountBefore2, counterRepo.count(), "No new counters should be saved on failure");
       assertEquals(companyCountBefore2, companyRepo.count(), "No new companies should be saved on failure");
       assertEquals(topLevelDomainCountBefore2, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
       assertEquals(tokenCountBefore2, tokenRepo.count(), "No new tokens should be saved on failure");

       // 4. Test that registration fails when token is missing for non-owner roles - FIXED PARAMETER ORDER
       // Test for ADMIN role
       UserRegisterRequest missingTokenAdminRequest = new UserRegisterRequest(
               "admin_user@example.com",            // email
               "adminUser",                         // username
               "password123",                       // password
               "firstName_" + i,
               "lastName_" + i,
               "middleName_" + i,
               existingCompany.getId(),             // companyId
           RoleManager.ADMIN_ROLE,              // role
           null                                 // roleToken
       );

       // Count repositories before attempting operation
       long userCountBefore3 = userRepo.count();
       long tokenUserLinkCountBefore3 = tokenUserLinkRepo.count();
       long companyUrlDataCountBefore3 = companyUrlDataRepo.count();
       long counterCountBefore3 = counterRepo.count();
       long companyCountBefore3 = companyRepo.count();
       long topLevelDomainCountBefore3 = topLevelDomainRepo.count();
       long tokenCountBefore3 = tokenRepo.count();

       assertThrows(
           TokenAndUserExceptions.MissingTokenException.class,
           () -> authCon.registerUser(missingTokenAdminRequest),
           "Should fail when token is missing for ADMIN role"
       );
       
       // Verify no objects were saved
       assertEquals(userCountBefore3, userRepo.count(), "No new users should be saved on failure");
       assertEquals(tokenUserLinkCountBefore3, tokenUserLinkRepo.count(), 
                   "No new token-user links should be saved on failure");
       assertEquals(companyUrlDataCountBefore3, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
       assertEquals(counterCountBefore3, counterRepo.count(), "No new counters should be saved on failure");
       assertEquals(companyCountBefore3, companyRepo.count(), "No new companies should be saved on failure");
       assertEquals(topLevelDomainCountBefore3, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
       assertEquals(tokenCountBefore3, tokenRepo.count(), "No new tokens should be saved on failure");

       // Test for EMPLOYEE role - FIXED PARAMETER ORDER
       UserRegisterRequest missingTokenEmployeeRequest = new UserRegisterRequest(
               "employee_user@example.com",         // email
               "employeeUser",                      // username
               "password123",                       // password
               "firstName_" + i,
               "lastName_" + i,
               "middleName_" + i,

               existingCompany.getId(),             // companyId
           RoleManager.EMPLOYEE_ROLE,           // role
           null
       );

       // Count repositories before attempting operation
       long userCountBefore4 = userRepo.count();
       long tokenUserLinkCountBefore4 = tokenUserLinkRepo.count();
       long companyUrlDataCountBefore4 = companyUrlDataRepo.count();
       long counterCountBefore4 = counterRepo.count();
       long companyCountBefore4 = companyRepo.count();
       long topLevelDomainCountBefore4 = topLevelDomainRepo.count();
       long tokenCountBefore4 = tokenRepo.count();

       assertThrows(
           TokenAndUserExceptions.MissingTokenException.class,
           () -> authCon.registerUser(missingTokenEmployeeRequest),
           "Should fail when token is missing for EMPLOYEE role"
       );
       
       // Verify no objects were saved
       assertEquals(userCountBefore4, userRepo.count(), "No new users should be saved on failure");
       assertEquals(tokenUserLinkCountBefore4, tokenUserLinkRepo.count(),
                   "No new token-user links should be saved on failure");
       assertEquals(companyUrlDataCountBefore4, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
       assertEquals(counterCountBefore4, counterRepo.count(), "No new counters should be saved on failure");
       assertEquals(companyCountBefore4, companyRepo.count(), "No new companies should be saved on failure");
       assertEquals(topLevelDomainCountBefore4, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
       assertEquals(tokenCountBefore4, tokenRepo.count(), "No new tokens should be saved on failure");
    }

    @Test
    void testNonOwnerSecondConstraints() {
        int i = 0;
        // 1. Create a verified company with email domain "example.com"
        Company verifiedCompany = new Company(
        "verified_company_id",
        "companyName",
        "companyAddress",
        "owner@example.com",
        "example.com",
        SubscriptionManager.getSubscription("TIER_1")
        );
        verifiedCompany.verify(); // Mark as verified
        companyRepo.save(verifiedCompany);

        i += 1;
        // 2. Test email domain mismatch - trying to register with gmail.com for example.com company
        UserRegisterRequest mismatchedEmailRequest = new UserRegisterRequest(
                "admin@gmail.com",               // email (mismatched domain)
                "newAdmin",                      // username
                "password123",                   // password
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                verifiedCompany.getId(),         // companyId (matching verified company)
           RoleManager.ADMIN_ROLE,          // role
           "admin_token_string"             // roleToken
        );

       // Count repositories before attempting operation
       long userCountBefore = userRepo.count();
       long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
       long companyUrlDataCountBefore = companyUrlDataRepo.count();
       long counterCountBefore = counterRepo.count();
       long companyCountBefore = companyRepo.count();
       long topLevelDomainCountBefore = topLevelDomainRepo.count();
       long tokenCountBefore = tokenRepo.count();


       assertThrows(
           CompanyAndUserExceptions.UserCompanyMisalignedException.class,
           () -> authCon.registerUser(mismatchedEmailRequest),
           "Should fail when email domain doesn't match company domain"
       );
       
       // Verify no objects were saved
       assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
       assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                   "No new token-user links should be saved on failure");
       assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
       assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
       assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
       assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
       assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");

       // 3. Test unverified company constraint
       // since registering an owner requires passing the email of the owner, we need to delete all users
       // to avoid the AlreadyExistingUserException
        //
       this.userRepo.deleteAll();

       for (Company company : companyRepo.findAll()) {
            // Skip verified companies
            if (company.getVerified()) {
                continue;
            }


            for (String roleString : RoleManager.ROLES_STRING) {
                    if (roleString.equals(RoleManager.OWNER_ROLE)) {
                        continue;
                    }

                    String domain = company.getEmailDomain();
                    String email = this.gen.randomAlphaString(10) + "@" + (domain == null ? "" : domain);


                    UserRegisterRequest unverifiedCompanyRequest = new UserRegisterRequest(
                        email,// email (matching domain)
                        "employee_" + company.getId(),         // username
                        "password123",                         // password
                        "firstName_" + i,
                        "lastName_" + i,
                        "middleName_" + i,
                        company.getId(),                       // companyId (unverified company)
                        roleString,             // role (non-owner)
                        "token_string_" + company.getId()      // roleToken
                    );

                    // Count repositories before attempting operation
                    long userCountBefore2 = userRepo.count();
                    long tokenUserLinkCountBefore2 = tokenUserLinkRepo.count();
                    long companyUrlDataCountBefore2 = companyUrlDataRepo.count();
                    long counterCountBefore2 = counterRepo.count();
                    long companyCountBefore2 = companyRepo.count();
                    long topLevelDomainCountBefore2 = topLevelDomainRepo.count();
                    long tokenCountBefore2 = tokenRepo.count();

                    assertThrows(
                        CompanyAndUserExceptions.UserBeforeOwnerException.class,
                        () -> authCon.registerUser(unverifiedCompanyRequest),
                        "Should fail when company is not verified yet"
                    );

                    // Verify no objects were saved
                    assertEquals(userCountBefore2, userRepo.count(), "No new users should be saved on failure");
                    assertEquals(tokenUserLinkCountBefore2, tokenUserLinkRepo.count(),
                                "No new token-user links should be saved on failure");
                    assertEquals(companyUrlDataCountBefore2, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
                    assertEquals(counterCountBefore2, counterRepo.count(), "No new counters should be saved on failure");
                    assertEquals(companyCountBefore2, companyRepo.count(), "No new companies should be saved on failure");
                    assertEquals(topLevelDomainCountBefore2, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
                    assertEquals(tokenCountBefore2, tokenRepo.count(), "No new tokens should be saved on failure");
            }
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testNonOwnerTokenConstraints() {
        setUp();
        // when a non-owner is registered his
        // 1. token must match with one of the tokens in the company + Role
        // cannot be deprecated or activated
        // 2. token cannot be linked to another user

        // Make all companies verified
        for (Company company : companyRepo.findAll()) {
            company.verify();
            companyRepo.save(company);
        }

        // Find YouTube and GitHub companies
        Company youtube = companyRepo.findById("aaa").get();
        Company github = companyRepo.findById("bbb").get();

        // Clear existing users to avoid conflicts
        userRepo.deleteAll();

        int i = 0;

        // 1. Test successful registration with admin token
        AppToken validAdminToken = new AppToken(
            "valid_admin_token",
            encoder().encode("valid_admin_token_string"),
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        tokenRepo.save(validAdminToken);

        i += 1;

        UserRegisterRequest validAdminRequest = new UserRegisterRequest(
                "new_admin@youtube.com",
                "new_admin",
                "password123",
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                youtube.getId(),
            RoleManager.ADMIN_ROLE,
            "valid_admin_token_string"
        );


        long userCountBefore = userRepo.count();
        long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
        long companyUrlDataCountBefore = companyUrlDataRepo.count();
        long counterCountBefore = counterRepo.count();
        long companyCountBefore = companyRepo.count();
        long topLevelDomainCountBefore = topLevelDomainRepo.count();
        long tokenCountBefore = tokenRepo.count();

        // This should succeed
        assertDoesNotThrow(
            () -> authCon.registerUser(validAdminRequest),
            "Should successfully register admin user with valid token"
        );

        assertEquals(userCountBefore + 1, userRepo.count(), "No new users should be saved on failure");
        assertEquals(tokenUserLinkCountBefore + 1, tokenUserLinkRepo.count(), 
                    "No new token-user links should be saved on failure");
        assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
        assertEquals(counterCountBefore, counterRepo.count(), "No new counters should be saved on failure");
        assertEquals(companyCountBefore, companyRepo.count(), "No new companies should be saved on failure");
        assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
        assertEquals(tokenCountBefore, tokenRepo.count(), "No new tokens should be saved on failure");

        // 2. Test mismatched role and token (admin token but employee role)
        AppToken adminTokenForEmployeeTest = new AppToken(
            "admin_token_for_employee_test",
            encoder().encode("admin_token_employee_string"),
            github,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        tokenRepo.save(adminTokenForEmployeeTest);


        UserRegisterRequest mismatchedRoleRequest = new UserRegisterRequest(
                "employee@github.com",
                "employee_with_admin_token",
                "password123",
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                github.getId(),
                RoleManager.EMPLOYEE_ROLE,
                "admin_token_employee_string"
        );

        // Count repositories before attempting operation
        long userCountBefore1 = userRepo.count();
        long tokenUserLinkCountBefore1 = tokenUserLinkRepo.count();
        long companyUrlDataCountBefore1 = companyUrlDataRepo.count();
        long counterCountBefore1 = counterRepo.count();
        long companyCountBefore1 = companyRepo.count();
        long topLevelDomainCountBefore1 = topLevelDomainRepo.count();
        long tokenCountBefore1 = tokenRepo.count();

        assertThrows(
            TokenAndUserExceptions.TokenNotFoundForRoleException.class,
            () -> authCon.registerUser(mismatchedRoleRequest),
            "Should fail when token role doesn't match requested role"
        );
        
        // Verify no objects were saved
        assertEquals(userCountBefore1, userRepo.count(), "No new users should be saved on failure");
        assertEquals(tokenUserLinkCountBefore1, tokenUserLinkRepo.count(), 
                    "No new token-user links should be saved on failure");
        assertEquals(companyUrlDataCountBefore1, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
        assertEquals(counterCountBefore1, counterRepo.count(), "No new counters should be saved on failure");
        assertEquals(companyCountBefore1, companyRepo.count(), "No new companies should be saved on failure");
        assertEquals(topLevelDomainCountBefore1, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
        assertEquals(tokenCountBefore1, tokenRepo.count(), "No new tokens should be saved on failure");

        // 3. Test with activated token
        AppToken activatedToken = new AppToken(
            "activated_token",
            encoder().encode("activated_token_string"),
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        activatedToken.activate(); // Activate the token
        tokenRepo.save(activatedToken);

        i += 1;

        UserRegisterRequest activatedTokenRequest = new UserRegisterRequest(
                "activated_admin@youtube.com",
                "admin_with_activated_token",
                "password123",

                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,

                youtube.getId(),
                RoleManager.ADMIN_ROLE,
            "activated_token_string"
        );

        // Count repositories before attempting operation
        long userCountBefore2 = userRepo.count();
        long tokenUserLinkCountBefore2 = tokenUserLinkRepo.count();
        long companyUrlDataCountBefore2 = companyUrlDataRepo.count();
        long counterCountBefore2 = counterRepo.count();
        long companyCountBefore2 = companyRepo.count();
        long topLevelDomainCountBefore2 = topLevelDomainRepo.count();
        long tokenCountBefore2 = tokenRepo.count();

        assertThrows(
            TokenAndUserExceptions.TokenAlreadyUsedException.class,
            () -> authCon.registerUser(activatedTokenRequest),
            "Should fail when token is already activated"
        );
        
        // Verify no objects were saved
        assertEquals(userCountBefore2, userRepo.count(), "No new users should be saved on failure");
        assertEquals(tokenUserLinkCountBefore2, tokenUserLinkRepo.count(),
                    "No new token-user links should be saved on failure");
        assertEquals(companyUrlDataCountBefore2, companyUrlDataRepo.count(), "No new company url data should be saved on failure");     
        assertEquals(counterCountBefore2, counterRepo.count(), "No new counters should be saved on failure");
        assertEquals(companyCountBefore2, companyRepo.count(), "No new companies should be saved on failure");
        assertEquals(topLevelDomainCountBefore2, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
        assertEquals(tokenCountBefore2, tokenRepo.count(), "No new tokens should be saved on failure");

        // 4. Test with expired/deprecated token
        AppToken expiredToken = new AppToken(
            "expired_token",
            encoder().encode("expired_token_string"),
            github,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        expiredToken.expire(); // Expire the token
        tokenRepo.save(expiredToken);

        i += 1;
        UserRegisterRequest expiredTokenRequest = new UserRegisterRequest(
                "expired_admin@github.com",
                "admin_with_expired_token",
                "password123",
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                github.getId(),
                RoleManager.ADMIN_ROLE,
                "expired_token_string"
        );

        // Count repositories before attempting operation
        long userCountBefore3 = userRepo.count();
        long tokenUserLinkCountBefore3 = tokenUserLinkRepo.count();
        long companyUrlDataCountBefore3 = companyUrlDataRepo.count();
        long counterCountBefore3 = counterRepo.count();
        long companyCountBefore3 = companyRepo.count();
        long topLevelDomainCountBefore3 = topLevelDomainRepo.count();
        long tokenCountBefore3 = tokenRepo.count();

        assertThrows(
            TokenAndUserExceptions.TokenExpiredException.class,
            () -> authCon.registerUser(expiredTokenRequest),
            "Should fail when token is expired"
        );
        
        // Verify no objects were saved
        assertEquals(userCountBefore3, userRepo.count(), "No new users should be saved on failure");
        assertEquals(tokenUserLinkCountBefore3, tokenUserLinkRepo.count(),
                    "No new token-user links should be saved on failure");
        assertEquals(companyUrlDataCountBefore3, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
        assertEquals(counterCountBefore3, counterRepo.count(), "No new counters should be saved on failure");
        assertEquals(companyCountBefore3, companyRepo.count(), "No new companies should be saved on failure");
        assertEquals(topLevelDomainCountBefore3, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
        assertEquals(tokenCountBefore3, tokenRepo.count(), "No new tokens should be saved on failure");

        // 5. Test with token already linked to a user
        AppToken linkedToken = new AppToken(
            "linked_token",
            encoder().encode("linked_token_string"),
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        linkedToken.activate();
        tokenRepo.save(linkedToken);

        // Create a user to link with the token
        AppUser existingUser = new AppUser(
            "existing_admin@youtube.com",
            "existing_admin",
            "password123",
            "firstName_" + i,
            "lastName_" + i,
            "middleName_" + i,
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        userRepo.save(existingUser);

        // Link the token to the existing user
        TokenUserLink tokenUserLink = new TokenUserLink(
            "link_id",
            linkedToken,
            existingUser
        );
        tokenUserLinkRepo.save(tokenUserLink);

        // Try to register another user with the same token
        UserRegisterRequest linkedTokenRequest = new UserRegisterRequest(
                "another_admin@youtube.com",
                "another_admin",
                "password123",
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                youtube.getId(),
                RoleManager.ADMIN_ROLE,
            "linked_token_string"
        );

        // Count repositories before attempting operation
        long userCountBefore4 = userRepo.count();
        long tokenUserLinkCountBefore4 = tokenUserLinkRepo.count();
        long companyUrlDataCountBefore4 = companyUrlDataRepo.count();
        long counterCountBefore4 = counterRepo.count();
        long companyCountBefore4 = companyRepo.count();
        long topLevelDomainCountBefore4 = topLevelDomainRepo.count();
        long tokenCountBefore4 = tokenRepo.count();

        assertThrows(
            TokenAndUserExceptions.TokenAlreadyUsedException.class,
            () -> authCon.registerUser(linkedTokenRequest),
            "Should fail when token is already linked to another user"
        );
        
        // Verify no objects were saved
        assertEquals(userCountBefore4, userRepo.count(), "No new users should be saved on failure");
        assertEquals(tokenUserLinkCountBefore4, tokenUserLinkRepo.count(),
                    "No new token-user links should be saved on failure");
        assertEquals(companyUrlDataCountBefore4, companyUrlDataRepo.count(), "No new company url data should be saved on failure");  
        assertEquals(counterCountBefore4, counterRepo.count(), "No new counters should be saved on failure");
        assertEquals(companyCountBefore4, companyRepo.count(), "No new companies should be saved on failure");
        assertEquals(topLevelDomainCountBefore4, topLevelDomainRepo.count(), "No new top level domains should be saved on failure");
        assertEquals(tokenCountBefore4, tokenRepo.count(), "No new tokens should be saved on failure");
    
    }

    @Test
    void testSuccessfulNonOwnerRegistration() {
        setUp();
        // 1. Make all companies verified
    for (Company company : companyRepo.findAll()) {
        company.verify();
        companyRepo.save(company);
    }

    // 2. Remove all existing users to avoid conflicts
    userRepo.deleteAll();
    tokenRepo.deleteAll();
    tokenUserLinkRepo.deleteAll();

    // Get companies to use in tests
    List<Company> companies = companyRepo.findAll();

    long companyCountBefore = companyRepo.count();
    long topLevelDomainCountBefore = topLevelDomainRepo.count(); 
    long companyUrlDataCountBefore = companyUrlDataRepo.count();
    long counterCountBefore = counterRepo.count();

    // Test for each non-owner role
    String[] roles = {RoleManager.ADMIN_ROLE, RoleManager.EMPLOYEE_ROLE};

    int offset = -10;
    for (String role : roles) {
        offset += 10;
        // Repeat registration test 5 times per role with random data
        for (int i = 0; i < 10; i++) {
                Company company = companies.get(i % companies.size());
                String randomSuffix = gen.randomAlphaString(5);

                // 1. Create token for the role
                String tokenId = "token_" + role + "_" + randomSuffix;
                String tokenString = "token_string_" + randomSuffix;
                String tokenHash = encoder().encode(tokenString);

                AppToken token = new AppToken(
                    tokenId,
                    tokenHash,
                    company,
                    RoleManager.getRole(role)
                );
                tokenRepo.save(token);

                // 2. Create registration request
                String email = role + "_" + randomSuffix + "@" + company.getEmailDomain();
                String username = role + "_user_" + randomSuffix;

                UserRegisterRequest request = new UserRegisterRequest(
                        email,
                        username,
                        "password123",
                        "firstName_" + i,
                        "lastName_" + i,
                        "middleName_" + i,
                        company.getId(),
                        role,
                    tokenString
                );

                // 3. Test successful registration (no exception)
                assertDoesNotThrow(() -> {
                    authCon.registerUser(request);
                }, "User registration should succeed for " + role + " role");

                // 4. Verify token is now active - using repository method
                Optional<AppToken> updatedTokenOpt = tokenRepo.findById(tokenId);
                assertTrue(updatedTokenOpt.isPresent(), "Token should exist after registration");
                assertEquals(
                    AppToken.TokenState.ACTIVE,
                    updatedTokenOpt.get().getTokenState(),
                    "Token should be active after registration"
                );

                // 5. Verify user was created - using repository method
                Optional<AppUser> createdUserOpt = userRepo.findById(email);
                assertTrue(createdUserOpt.isPresent(), "User should be created in repository");

                AppUser createdUser = createdUserOpt.get();
                assertEquals(username, createdUser.getUsername(), "Username should match request");
                assertEquals(role, createdUser.getRole().toString().toLowerCase(), "Role should match request");
                assertEquals(company.getId(), createdUser.getCompany().getId(), "Company should match request");

                // 6. Verify token-user link was created - using repository method
                AppToken updatedToken = updatedTokenOpt.get();
                List<TokenUserLink> links = tokenUserLinkRepo.findByToken(updatedToken);
                assertFalse(links.isEmpty(), "Token-user link should be created");

                // Alternative verification approach
                Optional<TokenUserLink> linkOpt = tokenUserLinkRepo.findByUserAndToken(createdUser, updatedToken);
                    assertTrue(linkOpt.isPresent(), "Should find token-user link for the specific user and token");

                // test the counts
                assertEquals(offset + i + 1, userRepo.count(), "No new users should be saved on failure");
                assertEquals(offset + i + 1, tokenUserLinkRepo.count(), "No new token-user links should be saved on failure");

                assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "register user does not save company url data");
                assertEquals(counterCountBefore, counterRepo.count(), "register user does not save counters");
                assertEquals(companyCountBefore, companyRepo.count(), "register user does not save companies");
                assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "register user does not save top level domains");
            }
        }
    }

    @Test
    void testOwnerRegistration() {                
        // 3. Test with mismatched owner email
        for (Company company : companyRepo.findAll()) {
            // Create email different from the company owner email
            String differentEmail = "different_" + gen.randomAlphaString(5) + "@" + company.getEmailDomain();
            
            UserRegisterRequest mismatchedOwnerRequest = new UserRegisterRequest(
                differentEmail,                       // Email
                "owner_" + gen.randomAlphaString(5),  // Username
                "password123",                        // Password
                "Owner",                              // First name
                "User",                               // Last name
                "Test",                               // Middle name
                company.getId(),                      // Company ID
                RoleManager.OWNER_ROLE,               // Role
                null                                  // Owner role doesn't need token
            );
            
            // Count repositories before attempting operation
            long userCountBefore = userRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();
            long companyCountBefore = companyRepo.count();
            long topLevelDomainCountBefore = topLevelDomainRepo.count();

            
            // test the counts
            assertThrows(
                CompanyAndUserExceptions.UserCompanyMisalignedException.class,
                () -> authCon.registerUser(mismatchedOwnerRequest),
                "Should fail when email doesn't match company owner email"
            );
            
            // Verify no objects were saved
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");

            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "register user does not save company url data");
            assertEquals(counterCountBefore, counterRepo.count(), "register user does not save counters");
            assertEquals(companyCountBefore, companyRepo.count(), "register user does not save companies");
            assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "register user does not save top level domains");
            
        }
        
        // Part 2: Test company verification constraint
        // Loop 10 times to create different verified companies
        for (int i = 0; i < 10; i++) {
            // Create a new company and verify it
            String companyId = "verified_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "owner@" + domain;
            
            Company verifiedCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            verifiedCompany.verify(); // Set to verified status
            companyRepo.save(verifiedCompany);
            
            // Try to register owner for already verified company
            UserRegisterRequest verifiedCompanyOwnerRequest = new UserRegisterRequest(
                    ownerEmail, // Matching owner email
                    "owner_" + i,
                    "password123",
                    "firstName_" + i,
                    "lastName_" + i,
                    "middleName_" + i,
                    companyId,
                    RoleManager.OWNER_ROLE,
                null // Owner role doesn't need token
            );
            
            // Count repositories before attempting operation
            long userCountBefore = userRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyUrlDataCountBefore = companyUrlDataRepo.count();
            long counterCountBefore = counterRepo.count();
            long companyCountBefore = companyRepo.count();
            long topLevelDomainCountBefore = topLevelDomainRepo.count();
            long tokenCountBefore = tokenRepo.count();

            assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> authCon.registerUser(verifiedCompanyOwnerRequest),
                "Should fail when company is already verified (has an owner)"
            );

            // test the counts
            assertEquals(userCountBefore, userRepo.count(), "No new users should be saved on failure");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "No new token-user links should be saved on failure");

            assertEquals(companyUrlDataCountBefore, companyUrlDataRepo.count(), "register user does not save company url data");
            assertEquals(counterCountBefore, counterRepo.count(), "register user does not save counters");
            assertEquals(companyCountBefore, companyRepo.count(), "register user does not save companies");
            assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "register user does not save top level domains");   
            assertEquals(tokenCountBefore, tokenRepo.count(), "register user does not save tokens");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulOwnerRegistration() {        
        // Test successful owner registration 10 times with different companies
        for (int i = 0; i < 10; i++) {
            String randomSuffix = gen.randomAlphaString(5);
            
            // 1. Create a new company using registerCompany method
            String companyId = "company_" + randomSuffix;
            String emailDomain = "domain" + i + ".com";
            String ownerEmail = "owner@" + emailDomain;
            String topLevelDomain = "www.company" + randomSuffix + ".com";

            // Create the company request
            CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
                companyId,
                    "companyName" + i,
                    "companyAdd" + i,
                    topLevelDomain,
                    ownerEmail,
                    emailDomain,
                    "TIER_1"
                    );
            
            // Register company - this should create the company but not an owner user yet
            assertDoesNotThrow(
                () -> authCon.registerCompany(companyRequest),
                "Company registration should succeed"
            );

            // extract the company from the companyRepo
            Company company = companyRepo.findById(companyId).get(); 

            // 6. Verify exactly one token exists for this company with owner role
            List<AppToken> ownerTokens = tokenRepo.findByCompanyAndRole(
                company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE)
            );

            assertEquals(1, ownerTokens.size(), 
                        "There should be exactly one owner token for the company");

            assertEquals(AppToken.TokenState.INACTIVE, ownerTokens.getFirst().getTokenState(),
                        "Owner token should be in INACTIVE state");

            // 2. Now register the owner for this company
            UserRegisterRequest ownerRequest = new UserRegisterRequest(
                    ownerEmail, // Matching owner email
                    "owner_" + i,
                    "password123",
                    "firstName_" + i,
                    "lastName_" + i,
                    "middleName_" + i,
                    companyId,
                    RoleManager.OWNER_ROLE,
                null // Owner role doesn't need token
            );
            
            // Owner registration should succeed
            assertDoesNotThrow(
                () -> authCon.registerUser(ownerRequest),
                "Owner registration should succeed for unverified company"
            );
            
            // 3. Verify user was created
            Optional<AppUser> ownerOpt = userRepo.findById(ownerEmail);
            assertTrue(ownerOpt.isPresent(), "Owner user should be created");
            
            AppUser owner = ownerOpt.get();
            assertEquals(RoleManager.OWNER_ROLE, owner.getRole().toString().toLowerCase(), 
                         "User should have owner role");
            assertEquals(companyId, owner.getCompany().getId(), 
                         "User should be associated with correct company");
            
            // 4. Verify company was created and is now verified
            Optional<Company> companyOpt = companyRepo.findById(companyId);
            assertTrue(companyOpt.isPresent(), "Company should exist");
            assertFalse(companyOpt.get().getVerified(),
                      "Owner registration is not enough for company verification");
            
            // the owner registration should not affect the token state whatsoever.
            ownerTokens = tokenRepo.findByCompanyAndRole(
                company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE)
            );

            assertEquals(1, ownerTokens.size(), 
                        "There should be exactly one owner token for the company");

            assertEquals(AppToken.TokenState.INACTIVE, ownerTokens.getFirst().getTokenState(),
                        "Owner token should be in INACTIVE state");

            AppToken ownerToken = ownerTokens.getFirst();
            // 6. Verify no token-user link exists for this token
            List<TokenUserLink> links = tokenUserLinkRepo.findByToken(ownerToken);
            assertTrue(links.isEmpty(), 
                      "There should be no token-user link for owner token");
        
            // test the counts

            assertEquals(i + 1, companyRepo.count(), "track companies count correctly");
            assertEquals(i + 1, topLevelDomainRepo.count(), "track top level domains count correctly");
            assertEquals(i + 1, companyUrlDataRepo.count(), "track company url data count correctly");

            assertEquals(i + 1, userRepo.count(), "track users count correctly");
            assertEquals(i + 1, tokenRepo.count(), "track tokens count correctly");

            assertEquals(0, tokenUserLinkRepo.count(), "No token-user links should be created without company verification");
            assertEquals(1, counterRepo.count(), "create only one counter: for the company data");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testVerifyCompanyInitialConstraints() {
        // 1. Test with a non-existing company
        for (int i = 0; i < 10; i++) {
            String nonExistentCompanyId = "__test_1_company_" + gen.randomAlphaString(8);
            CompanyVerifyRequest nonExistentCompanyRequest = new CompanyVerifyRequest(
                nonExistentCompanyId,
                "some_token",
                "owner@example.com"
            );
        
            // make sure the tokenUserLink count does not increase
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

            
            assertThrows(
                CompanyExceptions.NoCompanyException.class,
                () -> authCon.verifyCompany(nonExistentCompanyRequest),
                "Should fail when company ID doesn't exist"
            );
            
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");
        }
        
        // 2. Test with email different from company owner email
        // First, ensure we have an unverified company
        for (int i = 0; i < 10; i++) {
            String companyId = "__test_2_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "owner@" + domain;
            
            Company newCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            companyRepo.save(newCompany);

            // Create request with mismatched email
            String mismatchedEmail = "wrong_" + gen.randomAlphaString(5) + "@" + (newCompany.getEmailDomain() == null ? "" : newCompany.getEmailDomain());
            CompanyVerifyRequest mismatchedEmailRequest = new CompanyVerifyRequest(
                newCompany.getId(),
                "some_token",
                mismatchedEmail // Different from company.getOwnerEmail()
            );

            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

            assertThrows(
                CompanyAndUserExceptions.UserCompanyMisalignedException.class,
                () -> authCon.verifyCompany(mismatchedEmailRequest),
                "Should fail when email doesn't match company owner email"
            );
            
            // Verify no objects were saved
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");
            
            Company company = companyRepo.findById(companyId).get();
            // assert all fields are the same
            assertEquals(newCompany.getId(), company.getId(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyName(), company.getCompanyName(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyAddress(), company.getCompanyAddress(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getOwnerEmail(), company.getOwnerEmail(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getEmailDomain(), company.getEmailDomain(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getSubscription(), company.getSubscription(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getVerified(), company.getVerified(), "unsuccessful verification should not change the company state"); 

        }
                
        // 3. Test with already verified company
        for (int i = 0; i < 10; i++) {
            String companyId = "__test_3_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "owner@" + domain;
            
            Company newCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            newCompany.verify();            
            companyRepo.save(newCompany);

            // Create request with verified company
            CompanyVerifyRequest verifiedCompanyRequest = new CompanyVerifyRequest(
                companyId,
                "some_token",
                ownerEmail
            );
            
            // Count repositories before attempting operation
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

            assertThrows(
                CompanyExceptions.CompanyAlreadyVerifiedException.class,
                () -> authCon.verifyCompany(verifiedCompanyRequest),
                "Should fail when company is already verified"
            );
            
            // Verify no objects were saved
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure"); 

            Company company = companyRepo.findById(companyId).get();
            // assert all fields are the same
            assertEquals(newCompany.getId(), company.getId(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyName(), company.getCompanyName(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyAddress(), company.getCompanyAddress(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getOwnerEmail(), company.getOwnerEmail(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getEmailDomain(), company.getEmailDomain(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getSubscription(), company.getSubscription(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getVerified(), company.getVerified(), "unsuccessful verification should not change the company state");
        }

        // 4. Test with owner email that doesn't exist in the user repository
        
        // Create 10 new random companies and test verification
        for (int i = 0; i < 10; i++) {
            // Create a new unverified company
            String companyId = "__test_4_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "__no_existing_owner" + gen.randomAlphaString(5) + "@" + domain;
            
            Company newCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            companyRepo.save(newCompany);

            // Create verification request with correct owner email but no user exists
            CompanyVerifyRequest missingUserRequest = new CompanyVerifyRequest(
                companyId,
                "some_token_" + i,
                ownerEmail
            );
            
            // Count repositories before attempting operation
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

            // Test that verification fails because the user doesn't exist
            assertThrows(
                CompanyAndUserExceptions.UserBeforeOwnerException.class,
                () -> authCon.verifyCompany(missingUserRequest),
                "Should fail when owner hasn't registered as a user"
            );
            
            // Verify no objects were saved
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");

            Company company = companyRepo.findById(companyId).get();
            // assert all fields are the same
            assertEquals(newCompany.getId(), company.getId(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyName(), company.getCompanyName(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyAddress(), company.getCompanyAddress(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getOwnerEmail(), company.getOwnerEmail(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getEmailDomain(), company.getEmailDomain(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getSubscription(), company.getSubscription(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getVerified(), company.getVerified(), "unsuccessful verification should not change the company state");
        }
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testValidateOwnerToken() {
        for (int i = 0; i < 10; i++) {
            String companyId = "__test_5_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "owner@" + domain;
            
            Company newCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            companyRepo.save(newCompany);

            AppUser owner = new AppUser(
                ownerEmail,
                "ownerUser_" + i,
                "password123",
                    "firstName_" + i,
                    "lastName_" + i,
                    "middleName_" + i,
                    newCompany,
                RoleManager.getRole(RoleManager.OWNER_ROLE)
                );

            userRepo.save(owner);

            // do not create a token

            // create a company verify request
            CompanyVerifyRequest companyVerifyRequest = new CompanyVerifyRequest(
                companyId,
                "some_token_",
                ownerEmail
            );

            // Count repositories before attempting operation
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

            assertThrows(
                TokenAndUserExceptions.MissingTokenException.class,
                () -> authCon.verifyCompany(companyVerifyRequest),
                "Should fail when token doesn't exist"
            );
            
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), 
                        "No new token-user links should be saved on failure");

            Company company = companyRepo.findById(companyId).get();
            // assert all fields are the same
            assertEquals(newCompany.getId(), company.getId(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyName(), company.getCompanyName(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getCompanyAddress(), company.getCompanyAddress(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getOwnerEmail(), company.getOwnerEmail(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getEmailDomain(), company.getEmailDomain(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getSubscription(), company.getSubscription(), "unsuccessful verification should not change the company state");
            assertEquals(newCompany.getVerified(), company.getVerified(), "unsuccessful verification should not change the company state");

        }   
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void verifySuccessfulCompanyVerification() {
        for (int i = 0; i < 10; i++) {
            String companyId = "__test_6_company_" + gen.randomAlphaString(5);
            String domain = "domain" + i + ".com";
            String ownerEmail = "owner@" + domain;
            
            Company newCompany = new Company(
                companyId,
                "New Company " + i,                 // Company name
                "456 New Ave, Suite " + i,          // Company address
                ownerEmail,
                domain,
                SubscriptionManager.getSubscription("TIER_1")
            );
            companyRepo.save(newCompany);

            // create the owner user
            AppUser ownerUser = new AppUser(
                ownerEmail,
                "ownerUser_" + i,
                "password123",
                "firstName_" + i,
                "lastName_" + i,
                "middleName_" + i,
                newCompany,
                RoleManager.getRole(RoleManager.OWNER_ROLE)
            );  

            userRepo.save(ownerUser);

            // create a new token
            AppToken ownerToken = new AppToken(
                "owner_token_id" + i, 
                this.encoder().encode("owner_token_" + i),
                newCompany,
                RoleManager.getRole(RoleManager.OWNER_ROLE),
                null
            );

            tokenRepo.save(ownerToken);

            // create a company verify request
            CompanyVerifyRequest companyVerifyRequest = new CompanyVerifyRequest(
                companyId,
                "owner_token_" + i,
                ownerEmail
            );
            
            // verify the company
            assertDoesNotThrow(
                () -> authCon.verifyCompany(companyVerifyRequest),
                "Should succeed when company is verified"
            );            

            // 1. Verify the company is now verified
            Company updatedCompany = companyRepo.findById(companyId).get();
            assertTrue(updatedCompany.getVerified(), 
                      "Company should be verified after successful verification");
            
            // 2. Verify the token is now active
            AppToken updatedToken = tokenRepo.findById("owner_token_id" + i).get();
            assertEquals(AppToken.TokenState.ACTIVE, updatedToken.getTokenState(),
                        "Token should be active after successful verification");
            
            // 3. Verify a token-user link was created
            List<TokenUserLink> links = tokenUserLinkRepo.findByToken(updatedToken);
            assertFalse(links.isEmpty(), 
                       "A token-user link should be created after verification");
            
            assertEquals(1, links.size(),
                        "There should be exactly one token-user link");

            // Verify the link connects the correct user and token
            TokenUserLink link = links.getFirst();
            
            assertEquals(ownerEmail, link.getUser().getEmail(),
                        "Link should be associated with the correct user");
            
            assertEquals("owner_token_id" + i, link.getToken().getTokenId(),
            "Link should be associated with the correct token");
        }   
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testFullRegistrationFlow() throws IllegalAccessException, NoSuchFieldException {
        // Clear existing data to ensure clean test environment
        userRepo.deleteAll();
        tokenRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
        companyRepo.deleteAll();
        
        for (int i = 0; i < 10; i++) {
            String companyId = "full_flow_company_" + gen.randomAlphaString(5);
            String topLevelDomain = "www.top" + i + "LevelDomain" + ".com";
            String emailDomain = "flow" + i + ".com";
            String ownerEmail = "owner@" + emailDomain;
            
            // 1. Create and register a company
            CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
                companyId,
                "companyName_" + i,
                "companyAdd_" + i,
                topLevelDomain,
                ownerEmail,
                emailDomain,
                "TIER_1"
            );
            
            assertDoesNotThrow(
                () -> authCon.registerCompany(companyRequest),
                "Company registration should succeed"
            );
            
            // 2. Find the owner token that was created
            Company company = companyRepo.findById(companyId).get();
            assertNotNull(company, "Company should be created");
            assertFalse(company.getVerified(), "Company should not be verified yet");
            
            List<AppToken> ownerTokens = tokenRepo.findByCompanyAndRole(
                company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE)
            );
            
            assertEquals(1, ownerTokens.size(), "Should create exactly one owner token");
            AppToken ownerToken = ownerTokens.getFirst();
            assertEquals(AppToken.TokenState.INACTIVE, ownerToken.getTokenState(), "Token should be inactive initially");

            // 3. Register the owner user
            UserRegisterRequest ownerRequest = new UserRegisterRequest(
                    ownerEmail,
                    "owner_" + i,
                    "password123",
                    "firstName_" + i,
                    "lastName_" + i,
                    "middleName_" + i,
                    companyId,
                RoleManager.OWNER_ROLE,
                null // Owner doesn't need token
            );
            
            assertDoesNotThrow(
                () -> authCon.registerUser(ownerRequest),
                "Owner registration should succeed"
            );
            
            // Verify user was created
            Optional<AppUser> ownerOpt = userRepo.findById(ownerEmail);
            assertTrue(ownerOpt.isPresent(), "Owner user should be created");
//            AppUser owner = ownerOpt.get();

            // 4. Verify the company
            List<AppToken> tokens = tokenRepo.findByCompanyAndRole(
                company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE)
            );

            assertEquals(1, tokens.size(), "Should create exactly one owner token");
            ownerToken = tokens.getFirst();

            // modify the token hash using reflection
            // in the real application this is done by the email
            
            Field tokenHashField = AppToken.class.getDeclaredField("tokenHash");
            tokenHashField.setAccessible(true);
            tokenHashField.set(ownerToken, this.encoder().encode("owner_token_" + i));
            tokenRepo.save(ownerToken);

            String finalTokenString = "owner_token_" + i;

            CompanyVerifyRequest verifyRequest = new CompanyVerifyRequest(
                companyId,
                finalTokenString,
                ownerEmail
            );
            
            assertDoesNotThrow(
                () -> authCon.verifyCompany(verifyRequest),
                "Company verification should succeed"
            );
            
            // 5. Validate the final state
            // Check company is verified
            Company updatedCompany = companyRepo.findById(companyId).get();
            assertTrue(updatedCompany.getVerified(), 
                      "Company should be verified after the full flow");
            
            // Check token is active
            AppToken updatedToken = tokenRepo.findById(ownerToken.getTokenId()).get();
            assertEquals(AppToken.TokenState.ACTIVE, updatedToken.getTokenState(),
                        "Token should be active after verification");
            
            // Check token-user link was created
            List<TokenUserLink> links = tokenUserLinkRepo.findByToken(updatedToken);
            assertEquals(1, links.size(), 
                        "There should be exactly one token-user link");
            
            TokenUserLink link = links.getFirst();
            assertEquals(ownerEmail, link.getUser().getEmail(),
                        "Link should be associated with the correct user");
            assertEquals(ownerToken.getTokenId(), link.getToken().getTokenId(),
                        "Link should be associated with the correct token");
        }
    }
}
