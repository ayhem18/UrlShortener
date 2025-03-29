package org.tokenApi.tests;

import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.TokenAuthController;
import org.apiUtils.commonClasses.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.stubs.repositories.*;
import org.tokenApi.controllers.TokenController;
import org.tokenApi.exceptions.TokenExceptions;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.url.UrlProcessor;
import org.user.entities.AppUser;
import org.utils.CustomGenerator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class BaseTest {

    protected final StubCompanyRepo companyRepo;
    protected final StubCompanyUrlDataRepo companyUrlDataRepo;
    protected final StubTopLevelDomainRepo topLevelDomainRepo;
    protected final StubUserRepo userRepo;
    protected final StubUrlEncodingRepo urlEncodingRepo;
    protected final StubTokenRepo tokenRepo;  
    protected final StubTokenUserLinkRepo tokenUserLinkRepo;
    protected final CustomGenerator gen;
    protected final UrlProcessor urlProcessor;
    protected final PasswordEncoder encoder;

    public BaseTest() {
        companyRepo = new StubCompanyRepo();
        companyUrlDataRepo = new StubCompanyUrlDataRepo();
        topLevelDomainRepo = new StubTopLevelDomainRepo(companyRepo);
        userRepo = new StubUserRepo(companyRepo);
        urlEncodingRepo = new StubUrlEncodingRepo();
        tokenRepo = new StubTokenRepo(companyRepo);
        tokenUserLinkRepo = new StubTokenUserLinkRepo(this.tokenRepo, this.userRepo);

        gen = new CustomGenerator();
        urlProcessor = new UrlProcessor(gen);
        encoder = new BCryptPasswordEncoder();
    }


    protected void clear() {
        // Reset the repositories before each test
        companyRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll();
        urlEncodingRepo.deleteAll();        
        tokenRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
    }


    // Helper method to set up a test company with domains
    protected Company setUpCompany(String subscriptionName) {
        Subscription sub;

        if (subscriptionName.equalsIgnoreCase("test1")) {
            sub = new SubTest1();
        }
        else if (subscriptionName.equalsIgnoreCase("test2")) {
            sub = new SubTest2();
        }
        else {
            sub = SubscriptionManager.getSubscription(subscriptionName);
        }

        String companyId = gen.randomAlphaString(12);
        String companyName = gen.randomAlphaString(10);
        String companyEmailDomain = gen.randomAlphaString(5) + ".com";
        String companyEmail = "owner@" + companyEmailDomain;

        Company testCompany = new Company(
            companyId,
            companyName,
            gen.randomAlphaString(10),
            companyEmail,
            companyEmailDomain,
            sub
        );
        companyRepo.save(testCompany);

        // Create active domain
        String activeDomainName = "www." + companyName + "00active.com";
        TopLevelDomain activeDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            activeDomainName,
            testCompany
        );
        topLevelDomainRepo.save(activeDomain);
        
        // Create inactive domain
        String inactiveDomainName = "www." + companyName + "00inactive.com";
        TopLevelDomain inactiveDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            inactiveDomainName,
            testCompany
        );
        inactiveDomain.deactivate();
        topLevelDomainRepo.save(inactiveDomain);

        // Create deprecated domain
        String deprecatedDomainName = "www." + companyName + "00deprecated.com";
        TopLevelDomain deprecatedDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            deprecatedDomainName,
            testCompany
        );
        deprecatedDomain.deprecate();
        topLevelDomainRepo.save(deprecatedDomain);

        // Create company URL data
        CompanyUrlData companyUrlData = new CompanyUrlData(
            this.gen.randomAlphaString(20),
            testCompany,
            this.encoder.encode(activeDomainName).replaceAll("/", "_")
        );
        companyUrlDataRepo.save(companyUrlData);
        return testCompany;
    }

    // Helper method to set up a test user
    protected AppUser setUpUser(Company company, Role role, boolean authorized) {
        String username = "test_user_" + gen.randomAlphaString(10);
        String email = username + "@" + company.getEmailDomain();
        
        AppUser user = new AppUser(
            email, 
            username,
            "password123",
            "Test",
            "User",
            null,
            company,
            role
        );

        // create a token for the user
        AppToken token = new AppToken(gen.randomString(12), encoder.encode("tokenId"), company, role );
        token.activate();
        tokenRepo.save(token);

        if (authorized) {
            // create a token user link for the user
            TokenUserLink tokenUserLink = new TokenUserLink("link_id_" + username, token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        return userRepo.save(user);
    }
}


class GenerateTokenTest extends BaseTest {

    private final TokenController tokenController;

    public GenerateTokenTest() {
        super();
        tokenController = new TokenController(
            userRepo,
            tokenUserLinkRepo,
            tokenRepo
        );
    }

    @BeforeEach
    public void setUp() {
        super.clear();
    }        
    
    @AfterEach
    public void tearDown() {
        super.clear();
    }   

    @Test
    public void testUserWithNoToken() {    
        for (int i = 0; i < 10; i++) {
            // Setup company and unauthorized user (no token)
            Company company = setUpCompany("TIER_1");
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), false);
            UserDetails userDetails = new UserDetailsImp(user);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            long cUrlDataBefore = companyUrlDataRepo.count();

            // Execute and verify
            Exception exception = assertThrows(
                    TokenAuthController.TokenNotFoundException.class,
                    () -> tokenController.generateToken("EMPLOYEE", userDetails),
                    "Should throw TokenNotFoundException for user with no token"
            );
            
            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                    "Exception message should indicate missing token");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
            assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");
        }
    }

    @Test
    public void testRolesWithHigherPriority() {
        for (int i = 0; i < 10; i++) {
            // Setup company and users with different roles
            Company company = setUpCompany("test1");

            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser admin2 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser employee1 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            // Test invalid role combinations (lower role attempting to generate token for higher/equal role)
            // 1. Admin trying to generate Owner token
            testInvalidRoleCombination(admin1, RoleManager.OWNER_ROLE);
            
            // 2. Admin trying to generate Admin token
            testInvalidRoleCombination(admin1, RoleManager.ADMIN_ROLE);
            
            // 3. Employee trying to generate Owner token
            testInvalidRoleCombination(employee1, RoleManager.OWNER_ROLE);
            
            // 4. Employee trying to generate Admin token
            testInvalidRoleCombination(employee1, RoleManager.ADMIN_ROLE);
            
            // 5. Employee trying to generate Employee token
            testInvalidRoleCombination(employee1, RoleManager.EMPLOYEE_ROLE);

        }
    }
    
    private void testInvalidRoleCombination(AppUser user, String roleName) {
        UserDetails userDetails = new UserDetailsImp(user);
        
        // Capture database state before
        long tokenCountBefore = tokenRepo.count();
        long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
        long companyCountBefore = companyRepo.count();
        long cUrlDataBefore = companyUrlDataRepo.count();
        
        // Execute and verify
        Exception exception = assertThrows(
                TokenExceptions.InsufficientRoleAuthority.class,
                () -> tokenController.generateToken(roleName, userDetails),
                "Should throw InsufficientRoleAuthority when generating token for equal or higher role"
        );
        
        assertTrue(exception.getMessage().contains("Cannot generate token for role with equal or higher priority"),
                "Exception message should indicate insufficient role authority");
        
        // Verify no change in database state
        assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
        assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
        assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
        assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");
    }


    @Test
    public void testTokenLimit() {
        for (int i = 0; i < 10; i++) {
            // Create a company with TestSub subscription (which has limited users)
            Company company = setUpCompany("test2");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            UserDetails ownerDetails = new UserDetailsImp(owner);
            
            // First admin should work fine
            try {
                tokenController.generateToken(RoleManager.ADMIN_ROLE, ownerDetails);
            } catch (Exception e) {
                fail("First admin token generation should succeed: " + e.getMessage());
            }
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            long cUrlDataBefore = companyUrlDataRepo.count();
            
            // Second admin should fail
            Exception adminException = assertThrows(
                    TokenExceptions.NumTokensLimitExceeded.class,
                    () -> tokenController.generateToken(RoleManager.ADMIN_ROLE, ownerDetails),
                    "Should throw exception when exceeding admin token limit"
            );
            
            assertTrue(adminException.getMessage().contains("Maximum number of active tokens reached"),
                    "Exception message should indicate token limit exceeded");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            
            // First employee should work fine
            try {
                tokenController.generateToken(RoleManager.EMPLOYEE_ROLE, ownerDetails);
            } catch (Exception e) {
                fail("First employee token generation should succeed: " + e.getMessage());
            }
            
            // Capture database state again
            tokenCountBefore = tokenRepo.count();
            tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            // Second employee should fail
            Exception employeeException = assertThrows(
                    TokenExceptions.NumTokensLimitExceeded.class,
                    () -> tokenController.generateToken(RoleManager.EMPLOYEE_ROLE, ownerDetails),
                    "Should throw exception when exceeding employee token limit"
            );
            
            assertTrue(employeeException.getMessage().contains("Maximum number of active tokens reached"),
                    "Exception message should indicate token limit exceeded");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
            assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");
        }
    }

    @Test
    public void testSuccessfulTokenGeneration() {
        for (int i = 0; i < 10; i++) {
            // Setup company and owner
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            UserDetails ownerDetails = new UserDetailsImp(owner);
            
            // Capture state before token generation
            long userCountBefore = userRepo.count();
            long companyCountBefore = companyRepo.count();
            long topLevelDomainCountBefore = topLevelDomainRepo.count();
            long urlEncodingCountBefore = urlEncodingRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            try {
                // Generate employee token
                ResponseEntity<String> response = tokenController.generateToken(RoleManager.EMPLOYEE_ROLE, ownerDetails);
                
                // Verify response
                assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be 200 OK");
                
                // Verify token was created
                assertEquals(tokenCountBefore + 1, tokenRepo.count(), "Token count should increase by 1");
                
                // Verify other repositories didn't change
                assertEquals(userCountBefore, userRepo.count(), "User count should not change");
                assertEquals(companyCountBefore, companyRepo.count(), "Company count should not change");
                assertEquals(topLevelDomainCountBefore, topLevelDomainRepo.count(), "TopLevelDomain count should not change");
                assertEquals(urlEncodingCountBefore, urlEncodingRepo.count(), "UrlEncoding count should not change");
                assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
                
                // Verify generated token is for the right role and company
                List<AppToken> newTokens = tokenRepo.findByCompanyAndRole(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));
                assertFalse(newTokens.isEmpty(), "Should find at least one employee token");
                
                // The newest token should be the one we just created
                AppToken latestToken = newTokens.getLast();
                assertEquals(RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), latestToken.getRole(), "Token should have employee role");
                assertEquals(company.getId(), latestToken.getCompany().getId(), "Token should belong to the right company");
                assertEquals(AppToken.TokenState.INACTIVE, latestToken.getTokenState(), "Token should be in INACTIVE state");
                
            } catch (Exception e) {
                fail("Token generation should succeed: " + e.getMessage());
            }
        }
    }
}
    


