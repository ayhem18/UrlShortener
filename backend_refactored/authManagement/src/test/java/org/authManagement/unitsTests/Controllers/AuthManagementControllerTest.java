package org.authManagement.unitsTests.Controllers;

import org.authManagement.controllers.AuthController;
import org.authManagement.exceptions.CompanyAndUserExceptions;
import org.authManagement.exceptions.CompanyExceptions;
import org.authManagement.exceptions.TokenAndUserExceptions;
import org.authManagement.exceptions.UserExceptions;
import org.authManagement.internal.StubCompanyRepo;
import org.authManagement.internal.StubCounterRepo;
import org.authManagement.internal.StubTokenRepo;
import org.authManagement.internal.StubTokenUserLinkRepo;
import org.authManagement.internal.StubTopLevelDomainRepo;
import org.authManagement.internal.StubUserRepo;
import org.authManagement.requests.CompanyRegisterRequest;
import org.authManagement.requests.UserRegisterRequest;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;
import org.utils.CustomGenerator;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.access.Role;
import org.access.RoleManager;
import org.access.SubscriptionManager;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthManagementControllerTest {

    private final StubCompanyRepo companyRepo;
    private final StubCounterRepo counterRepo;
    private final StubUserRepo userRepo;
    private final StubTopLevelDomainRepo topLevelDomainRepo;
    private final StubTokenRepo tokenRepo;
    private final StubTokenUserLinkRepo tokenUserLinkRepo;

    private final CustomGenerator gen = new CustomGenerator();
    private final CustomGenerator mockGen;
    private final AuthController authCon;
    public static int COUNTER = 0;

    private CustomGenerator setMockCG() {
        CustomGenerator mockedCG = spy();
        when(mockedCG.randomString(anyInt())).then(
          invocation -> {
              AuthManagementControllerTest.COUNTER += 1; return "RANDOM_STRING_" + AuthManagementControllerTest.COUNTER;}
        );
        return mockedCG;
    }

    @BeforeEach
    public void setUp() {        
        // Clear repositories to ensure a clean state
        clearRepositories();
        
        // Initialize default test data for each repository
        
        // 1. Add default companies
        Company youtube = new Company("aaa", SubscriptionManager.getSubscription("TIER_1"), "owner@youtube.com", "youtube.com");
        Company github = new Company("bbb", SubscriptionManager.getSubscription("TIER_1"), "owner@github.com", "github.com");
        companyRepo.save(youtube);
        companyRepo.save(github);
        
        // 2. Add default domains for these companies
        TopLevelDomain youtubeDomain = new TopLevelDomain("domain1", "youtube.com", "hash_youtube", youtube);
        TopLevelDomain githubDomain = new TopLevelDomain("domain2", "github.com", "hash_github", github);
        topLevelDomainRepo.save(youtubeDomain);
        topLevelDomainRepo.save(githubDomain);
        
        // 3. Add default users for these companies
        AppUser youtubeOwner = new AppUser("owner@youtube.com", "ytowner", "password123", youtube, RoleManager.getRole(RoleManager.OWNER_ROLE));
        AppUser githubOwner = new AppUser("owner@github.com", "ghowner", "password123", github, RoleManager.getRole(RoleManager.OWNER_ROLE));
        
        AppUser youtubeAdmin = new AppUser("admin@youtube.com", "ytadmin", "password123", youtube, RoleManager.getRole(RoleManager.ADMIN_ROLE));
        AppUser githubAdmin = new AppUser("admin@github.com", "ghadmin", "password123", github, RoleManager.getRole(RoleManager.ADMIN_ROLE));

        AppUser youtubeEmployee = new AppUser("employee@youtube.com", "ytemployee", "password123", youtube, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));
        AppUser githubEmployee = new AppUser("employee@github.com", "ghemployee", "password123", github, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));

        userRepo.saveAll(List.of(youtubeOwner, githubOwner, youtubeAdmin, githubAdmin, youtubeEmployee, githubEmployee));

        // 6. Ensure counter repository is initialized
        counterRepo.addCompanyCollection();
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
        // The counter repo might need special handling if it doesn't have a clear method
        // We might need to reset it to initial state instead
    }

    public AuthManagementControllerTest() throws NoSuchFieldException, IllegalAccessException {
        this.companyRepo = new StubCompanyRepo();
        this.topLevelDomainRepo = new StubTopLevelDomainRepo(this.companyRepo);
        this.counterRepo = new StubCounterRepo();
        this.userRepo = new StubUserRepo(this.companyRepo);
        this.tokenRepo = new StubTokenRepo(this.companyRepo);
        this.tokenUserLinkRepo = new StubTokenUserLinkRepo(this.tokenRepo, this.userRepo);
        
        
        this.mockGen = setMockCG();
        // set a stubCustomGenerator, so we can verify the registerCompany method properly
        
        this.authCon = new AuthController(this.companyRepo,
                this.topLevelDomainRepo,
                this.userRepo,
                this.counterRepo,
                this.tokenRepo,
                this.tokenUserLinkRepo,
                this.gen,
                null);
    }

   // Helper method for password encoding
    private PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    //////////////////////// register a company ////////////////////////
    @Test
    void testUniquenessConstraints() {
        // 1. company ID uniqueness test 
        for (Company c : this.companyRepo.findAll()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(c.getId(),
                    "random_domain.com",
                    c.getSubscription().getTier(),
                    null,
                    null);

            Assertions.assertThrows(
                    CompanyExceptions.ExistingCompanyException.class,
                    () -> this.authCon.registerCompany(req)
            );
        }

        // 2. top level domain uniqueness test
        for (TopLevelDomain domain : this.topLevelDomainRepo.findAll()) {
            // Generate a unique company ID for this test case
            String uniqueId = "unique_" + this.gen.randomString(8);

            // Create a request with unique ID but existing domain
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    domain.getDomain(),
                    "TIER_1",
                    "owner@" + domain.getDomain(),  // Use matching email for domain
                    domain.getDomain()
            );

            // Verify that an ExistingTopLevelDomainException is thrown
            Assertions.assertThrows(
                    CompanyExceptions.ExistingTopLevelDomainException.class,
                    () -> this.authCon.registerCompany(req)
            );
        }

        // 3. owner email uniqueness test
        for (AppUser user : this.userRepo.findAll()) {
            // Get the user's email
            String userEmail = user.getEmail();

            // Extract domain from email for consistent request
            String emailDomain = userEmail.substring(userEmail.indexOf('@') + 1);

            // Generate unique domain and ID
            String uniqueId = "unique_" + this.gen.randomString(8);
            String uniqueDomain = "unique" + this.gen.randomString(5) + ".com";

            // Create request with unique ID, unique domain, but existing email
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    uniqueDomain,
                    "TIER_1",
                    userEmail,  // Existing user email
                    emailDomain
            );

            // Verify that a MultipleOwnersException is thrown
            Assertions.assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> this.authCon.registerCompany(req)
            );
        }
    }

    @Test 
    void testTokenConstraintsRegisterCompany() {
        
        for (String roleString : RoleManager.ROLES_STRING) {
            // 1. Generate a random company id that doesn't exist in the repo
            String randomCompanyId = "test_company_" + gen.randomAlphaString(8);
            while (companyRepo.existsById(randomCompanyId)) {
                randomCompanyId = "test_company_" + gen.randomAlphaString(8);
            }
            
            // Create a dummy company to associate with the token
            Company dummyCompany = new Company(
                randomCompanyId,
                SubscriptionManager.getSubscription("TIER_1"),
                "owner@example.com",
                "example.com"
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
                "unique" + gen.randomAlphaString(5) + ".com",
                "TIER_1",
                "newowner@example.com",
                "example.com"
            );
            
            // Final randomCompanyId for use in lambda
            String finalCompanyId = randomCompanyId;
            
            // Verify that registering a company with this ID throws an exception
            assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> authCon.registerCompany(req),
                "Should fail when token already exists for company ID " + finalCompanyId
            );
        }
    }

    @Test
    void testRegisterCompany() {
        // 1. Generate a random company id that doesn't exist in the repo
        String randomCompanyId = "new_company_" + gen.randomAlphaString(8);
        while (companyRepo.existsById(randomCompanyId)) {
            randomCompanyId = "new_company_" + gen.randomAlphaString(8);
        }
        
        // Generate unique domain
        String domain = "unique" + gen.randomAlphaString(5) + ".com";
        String ownerEmail = "owner@" + domain;
        
        // 2. Create and submit a company registration request
        CompanyRegisterRequest req = new CompanyRegisterRequest(
            randomCompanyId,
            domain,
            "TIER_1",
            ownerEmail,
            domain
        );
        
        // Register the company and verify no exceptions are thrown
        assertDoesNotThrow(() -> authCon.registerCompany(req));
        
        // 3. Verify that a company with this ID now exists
        assertTrue(companyRepo.existsById(randomCompanyId), 
            "Company should exist in repository after registration");
        
        // Get the newly created company
        Company newCompany = companyRepo.findById(randomCompanyId).orElse(null);
        assertNotNull(newCompany, "Company should not be null");
        
        // Verify company properties
        assertEquals(randomCompanyId, newCompany.getId());
        assertEquals(domain, newCompany.getEmailDomain());
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
    }

    //////////////////////// register a user ////////////////////////

    @Test
    void testRegisterUserInitialConstraints() {
       // 1. Test that registration fails when the username already exists
       // Loop through all existing users to test constraint
       for (AppUser existingUser : userRepo.findAll()) {

           // Create request with existing username - FIXED PARAMETER ORDER
           UserRegisterRequest existingUserRequest = new UserRegisterRequest(
               existingUser.getCompany().getId(),    // companyId
               "someUsername",                       // username
               "password123",                        // password
                existingUser.getEmail()   ,          // email
               RoleManager.EMPLOYEE_ROLE,            // role
               "someToken"                           // roleToken
           );

           assertThrows(
               UserExceptions.AlreadyExistingUserException.class,
               () -> authCon.registerUser(existingUserRequest),
               "Should fail when a user with the email already exists: " + existingUser.getEmail()
           );
       }

       // 2. Test that registration fails when the company does not exist - FIXED PARAMETER ORDER
       String nonExistentCompanyId = "non_existent_company_" + gen.randomAlphaString(8);
       UserRegisterRequest nonExistentCompanyRequest = new UserRegisterRequest(
           nonExistentCompanyId,                // companyId
           "newUser",                           // username
           "password123",                       // password
           "new_user@example.com",              // email
           RoleManager.EMPLOYEE_ROLE,           // role
           "someToken"                          // roleToken
       );

       assertThrows(
           UserExceptions.UserWithNoCompanyException.class,
           () -> authCon.registerUser(nonExistentCompanyRequest),
           "Should fail when the company ID doesn't exist"
       );

       // 3. Test that registration fails when the role does not exist - FIXED PARAMETER ORDER
       String invalidRole = "INVALID_ROLE_";
       Company existingCompany = companyRepo.findAll().getFirst();
       UserRegisterRequest invalidRoleRequest = new UserRegisterRequest(
           existingCompany.getId(),             // companyId
           "newUser",                           // username
           "password123",                       // password
           "new_user@example.com",              // email
           invalidRole,                         // role
           "someToken"                          // roleToken
       );

       assertThrows(
           RoleManager.NoExistingRoleException.class,
           () -> authCon.registerUser(invalidRoleRequest),
           "Should fail when the role doesn't exist"
       );

       // 4. Test that registration fails when token is missing for non-owner roles - FIXED PARAMETER ORDER
       // Test for ADMIN role
       UserRegisterRequest missingTokenAdminRequest = new UserRegisterRequest(
           existingCompany.getId(),             // companyId
           "adminUser",                         // username
           "password123",                       // password
           "admin_user@example.com",            // email
           RoleManager.ADMIN_ROLE,              // role
           null                                 // roleToken
       );

       assertThrows(
           TokenAndUserExceptions.MissingTokenException.class,
           () -> authCon.registerUser(missingTokenAdminRequest),
           "Should fail when token is missing for ADMIN role"
       );

       // Test for EMPLOYEE role - FIXED PARAMETER ORDER
       UserRegisterRequest missingTokenEmployeeRequest = new UserRegisterRequest(
           existingCompany.getId(),             // companyId
           "employeeUser",                      // username
           "password123",                       // password
           "employee_user@example.com",         // email
           RoleManager.EMPLOYEE_ROLE,           // role
           null
       );

       assertThrows(
           TokenAndUserExceptions.MissingTokenException.class,
           () -> authCon.registerUser(missingTokenEmployeeRequest),
           "Should fail when token is missing for EMPLOYEE role"
       );
    }

    @Test
    void testNonOwnerSecondConstraints() {
       // 1. Create a verified company with email domain "example.com"
       Company verifiedCompany = new Company(
           "verified_company_id",
           SubscriptionManager.getSubscription("TIER_1"),
           "owner@example.com",
           "example.com"
       );
       verifiedCompany.verify(); // Mark as verified
       companyRepo.save(verifiedCompany);

       // 2. Test email domain mismatch - trying to register with gmail.com for a example.com company
       UserRegisterRequest mismatchedEmailRequest = new UserRegisterRequest(
           verifiedCompany.getId(),         // companyId (matching verified company)
           "newAdmin",                      // username
           "password123",                   // password
           "admin@gmail.com",               // email (mismatched domain)
           RoleManager.ADMIN_ROLE,          // role
           "admin_token_string"             // roleToken
       );

       assertThrows(
           CompanyAndUserExceptions.UserCompanyMisalignedException.class,
           () -> authCon.registerUser(mismatchedEmailRequest),
           "Should fail when email domain doesn't match company domain"
       );

       // 3. Test unverified company constraint
       // since registering an owner requires passing the email of the owner, we need to delete all users
       // to avoid the AlreadyExistingUserException

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
                    company.getId(),                       // companyId (unverified company)
                    "employee_" + company.getId(),         // username
                    "password123",                         // password
                    email,// email (matching domain)
                    roleString,             // role (non-owner)
                    "token_string_" + company.getId()      // roleToken
                    );

                    assertThrows(
                        CompanyAndUserExceptions.UserBeforeOwnerException.class,
                        () -> authCon.registerUser(unverifiedCompanyRequest),
                        "Should fail when company is not verified yet"
                    );
            }
        }
    }

    @Test
    void testNonOwnerTokenConstraints() {
        // when a non-owner is registered his
        // 1. token must match with one of the tokens in the company + Role
        // cannot be deprecated or activated
        // 2. token cannot be linked to another user

        // Make all companies verified
        for (Company company : companyRepo.findAll()) {
            company.verify();
            companyRepo.save(company);
        }

        // Find youtube and github companies
        Company youtube = companyRepo.findById("aaa").get();
        Company github = companyRepo.findById("bbb").get();

        // Clear existing users to avoid conflicts
        userRepo.deleteAll();

        // 1. Test successful registration with admin token
        AppToken validAdminToken = new AppToken(
            "valid_admin_token",
            encoder().encode("valid_admin_token_string"),
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        tokenRepo.save(validAdminToken);

        UserRegisterRequest validAdminRequest = new UserRegisterRequest(
            youtube.getId(),
            "new_admin",
            "password123",
            "new_admin@youtube.com",
            RoleManager.ADMIN_ROLE,
            "valid_admin_token_string"
        );

        // This should succeed
        assertDoesNotThrow(
            () -> authCon.registerUser(validAdminRequest),
            "Should successfully register admin user with valid token"
        );

        // 2. Test mismatched role and token (admin token but employee role)
        AppToken adminTokenForEmployeeTest = new AppToken(
            "admin_token_for_employee_test",
            encoder().encode("admin_token_employee_string"),
            github,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        tokenRepo.save(adminTokenForEmployeeTest);

        UserRegisterRequest mismatchedRoleRequest = new UserRegisterRequest(
            github.getId(),
            "employee_with_admin_token",
            "password123",
            "employee@github.com",
            RoleManager.EMPLOYEE_ROLE,
            "admin_token_employee_string"
        );

        assertThrows(
            TokenAndUserExceptions.TokenNotFoundForRoleException.class,
            () -> authCon.registerUser(mismatchedRoleRequest),
            "Should fail when token role doesn't match requested role"
        );

        // 3. Test with activated token
        AppToken activatedToken = new AppToken(
            "activated_token",
            encoder().encode("activated_token_string"),
            youtube,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        activatedToken.activate(); // Activate the token
        tokenRepo.save(activatedToken);

        UserRegisterRequest activatedTokenRequest = new UserRegisterRequest(
            youtube.getId(),
            "admin_with_activated_token",
            "password123",
            "activated_admin@youtube.com",
            RoleManager.ADMIN_ROLE,
            "activated_token_string"
        );

        assertThrows(
            TokenAndUserExceptions.TokenAlreadyUsedException.class,
            () -> authCon.registerUser(activatedTokenRequest),
            "Should fail when token is already activated"
        );

        // 4. Test with expired/deprecated token
        AppToken expiredToken = new AppToken(
            "expired_token",
            encoder().encode("expired_token_string"),
            github,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        expiredToken.expire(); // Expire the token
        tokenRepo.save(expiredToken);

        UserRegisterRequest expiredTokenRequest = new UserRegisterRequest(
            github.getId(),
            "admin_with_expired_token",
            "password123",
            "expired_admin@github.com",
            RoleManager.ADMIN_ROLE,
            "expired_token_string"
        );

        assertThrows(
            TokenAndUserExceptions.TokenExpiredException.class,
            () -> authCon.registerUser(expiredTokenRequest),
            "Should fail when token is expired"
        );

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
            youtube.getId(),
            "another_admin",
            "password123",
            "another_admin@youtube.com",
            RoleManager.ADMIN_ROLE,
            "linked_token_string"
        );

        assertThrows(
            TokenAndUserExceptions.TokenAlreadyUsedException.class,
            () -> authCon.registerUser(linkedTokenRequest),
            "Should fail when token is already linked to another user"
        );
    }

    @Test
    void testSuccessfulUserRegistration() {
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

    // Test for each non-owner role
    String[] roles = {RoleManager.ADMIN_ROLE, RoleManager.EMPLOYEE_ROLE};

    for (String role : roles) {
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
                company.getId(),
                username,
                "password123",
                email,
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
        }
    }
    }

    
}
