package org.tokenApi.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.TokenAuthController;
import org.apiUtils.commonClasses.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.CompanyUrlDataRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tokenApi.controllers.TokenController;
import org.tokenApi.exceptions.TokenExceptions;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.tokenApi.configurations.TokenApiIntegrationTestConfig;
import org.user.entities.AppUser;
import org.user.repositories.UrlEncodingRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest(classes = IntegrationTestConfig.class)
class IntegrationBaseTest {

    protected TestRestTemplate restTemplate;
    protected CompanyRepository companyRepo;
    protected CompanyUrlDataRepository companyUrlDataRepo;
    protected TopLevelDomainRepository topLevelDomainRepo;
    protected UserRepository userRepo;
    protected UrlEncodingRepository urlEncodingRepo;
    protected TokenRepository tokenRepo;
    protected TokenUserLinkRepository tokenUserLinkRepo; 
    protected CustomGenerator gen;

	protected TokenController tokenController;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final PasswordEncoder encoder = new BCryptPasswordEncoder();


    @Autowired
    public IntegrationBaseTest(
        TestRestTemplate restTemplate,
        CompanyRepository companyRepo,
        CompanyUrlDataRepository companyUrlDataRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        UserRepository userRepo,
        UrlEncodingRepository urlEncodingRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        CustomGenerator gen,
    	TokenController tokenController
	) {
        this.restTemplate = restTemplate;
        this.companyRepo = companyRepo;
        this.companyUrlDataRepo = companyUrlDataRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.userRepo = userRepo;
        this.urlEncodingRepo = urlEncodingRepo; 
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.gen = gen;
		this.tokenController = tokenController;
    }

    
    @BeforeEach
    @AfterEach
    protected void clear() {
        // Reset the repositories before each test
        urlEncodingRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
        tokenRepo.deleteAll();
        userRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        companyRepo.deleteAll();
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
        String password = gen.randomAlphaString(12);
        AppUser user = new AppUser(
            email, 
            username,
            encoder.encode(password),
            "Test",
            "User",
            null,
            company,
            role
        );

        userRepo.save(user);


        if (authorized) {
            // create a token for the user
            AppToken token = new AppToken(gen.randomString(12), encoder.encode("tokenId"), company, role );
            token.activate();
            tokenRepo.save(token);

            // create a token user link for the user
            TokenUserLink tokenUserLink = new TokenUserLink("link_id_" + username, token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        user.setPassword(password);
        return user;
    }

    // Helper method to create HTTP headers for authentication
    protected HttpHeaders createAuthHeaders(AppUser user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create HTTP Basic Auth header
        String auth = user.getEmail() + ":" + user.getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
}



@SpringBootTest(classes = TokenApiIntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationGenerateTokenTest extends IntegrationBaseTest {

    @Autowired
    public IntegrationGenerateTokenTest(TestRestTemplate restTemplate,
                           CompanyRepository companyRepo,
                           CompanyUrlDataRepository companyUrlDataRepo, 
                           TopLevelDomainRepository topLevelDomainRepo,
                           UserRepository userRepo, 
                           UrlEncodingRepository urlEncodingRepo, 
                           TokenRepository tokenRepo,  
                           TokenUserLinkRepository tokenUserLinkRepo, 
                           CustomGenerator gen,
                           TokenController tokenController) {
        super(restTemplate, companyRepo, companyUrlDataRepo, topLevelDomainRepo, 
              userRepo, urlEncodingRepo, tokenRepo, tokenUserLinkRepo, gen, tokenController);
    }

    @SuppressWarnings("null")
    @Test
    public void testUserWithNoToken() {
        for (int i = 0; i < 10; i++) {
            // Setup company and unauthorized user (no token)
            Company company = setUpCompany("TIER_1");
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), false);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            long cUrlDataBefore = companyUrlDataRepo.count();


            // Create auth headers for user with no token
            HttpHeaders headers = createAuthHeaders(user);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Execute request and verify response
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.EMPLOYEE_ROLE,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response status is FORBIDDEN (403) for a user with
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), 
                    "User with no token should get FORBIDDEN status");
            
            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("Their access might have been revoked"),
                    "Error message should indicate missing token");
            
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
            Company company = setUpCompany("TIER_1");
            AppUser admin = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser employee = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            // Test invalid role combinations

            testInvalidRoleCombinationForGenerate(admin, RoleManager.OWNER_ROLE);
            testInvalidRoleCombinationForGenerate(admin, RoleManager.ADMIN_ROLE);

            // employees are unauthorized to call this endpoint since they do not have the CAN_WORK_WITH_TOKENS authority
            testInvalidRoleCombinationForGenerate(employee, RoleManager.OWNER_ROLE);
            testInvalidRoleCombinationForGenerate(employee, RoleManager.ADMIN_ROLE);
            testInvalidRoleCombinationForGenerate(employee, RoleManager.EMPLOYEE_ROLE);
        }
    }
    
    @SuppressWarnings("null")
    private void testInvalidRoleCombinationForGenerate(AppUser user, String roleName) {
        // Capture database state before
        long tokenCountBefore = tokenRepo.count();
        long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
        long companyCountBefore = companyRepo.count();
        long cUrlDataBefore = companyUrlDataRepo.count();
        
        // Create auth headers
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Execute request and verify response
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/token/generate?role=" + roleName,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        if (user.getRole().role().equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                    "Should return BAD_REQUEST for invalid role combination");
            // this is set by the spring security
        }
        else {
            // Verify response status is BAD_REQUEST (400) for invalid role combination
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                    "Should return BAD_REQUEST for invalid role combination");

            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("Cannot generate token for role with equal or higher priority"),
                    "Error message should indicate insufficient role authority");
        }

        // Verify no change in database state
        assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
        assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
        assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
        assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");
    }

    @SuppressWarnings("null")
    @Test
    public void testTokenLimit() {
        for (int i = 0; i < 10; i++) {
            // Setup company with test subscription (limited to 1 admin and 1 employee)
            Company company = setUpCompany("test2");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            
            // 1. Generate first admin token - should succeed
            HttpHeaders ownerHeaders = createAuthHeaders(owner);
            HttpEntity<Void> ownerRequest = new HttpEntity<>(ownerHeaders);
            
            ResponseEntity<String> response1 = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.ADMIN_ROLE,
                    HttpMethod.GET,
                    ownerRequest,
                    String.class
            );
            
            assertEquals(HttpStatus.OK, response1.getStatusCode(), 
                    "First admin token generation should succeed");
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            long cUrlDataBefore = companyUrlDataRepo.count();


            // 2. Generate second admin token - should fail due to limit
            ResponseEntity<String> response2 = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.ADMIN_ROLE,
                    HttpMethod.GET,
                    ownerRequest,
                    String.class
            );
            
            assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode(), 
                    "Second admin token generation should fail due to limit");
            assertTrue(Objects.requireNonNull(response2.getBody()).contains("Maximum number of active tokens reached"),
                    "Error message should indicate token limit reached");

                    
                                // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
            assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");

                    
            // 3. Generate first employee token - should succeed
            ResponseEntity<String> response3 = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.EMPLOYEE_ROLE,
                    HttpMethod.GET,
                    ownerRequest,
                    String.class
            );
            
            assertEquals(HttpStatus.OK, response3.getStatusCode(), 
                    "First employee token generation should succeed");
            
            // Capture database state again
            tokenCountBefore = tokenRepo.count();
            tokenUserLinkCountBefore = tokenUserLinkRepo.count();


            // 4. Generate second employee token - should fail due to limit
            ResponseEntity<String> response4 = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.EMPLOYEE_ROLE,
                    HttpMethod.GET,
                    ownerRequest,
                    String.class
            );
            
            assertEquals(HttpStatus.BAD_REQUEST, response4.getStatusCode(), 
                    "Second employee token generation should fail due to limit");
            assertTrue(Objects.requireNonNull(response4.getBody()).contains("Maximum number of active tokens reached"),
                    "Error message should indicate token limit reached");


            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
            assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company count should not change");

        }
    }

    @Test
    public void testSuccessfulTokenGeneration() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // Setup company and authorized owner
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long userCountBefore = userRepo.count();
            long companyCountBefore = companyRepo.count();
            long topLevelDomainCountBefore = topLevelDomainRepo.count();
            long urlEncodingCountBefore = urlEncodingRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            // Create auth headers
            HttpHeaders headers = createAuthHeaders(owner);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Generate employee token
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/generate?role=" + RoleManager.EMPLOYEE_ROLE,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be 200 OK");
            
            // Parse response to get token
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            assertTrue(jsonResponse.has("token"), "Response should contain token field");
            
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
            
            AppToken latestToken = newTokens.getLast();
            assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), latestToken.getRole().role().toLowerCase(), "Token should have employee role");
            assertEquals(company.getId(), latestToken.getCompany().getId(), "Token should belong to the right company");
            assertEquals(AppToken.TokenState.INACTIVE, latestToken.getTokenState(), "Token should be in INACTIVE state");
        }
    }
}


@SpringBootTest(classes = TokenApiIntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationRevokeTokenTest extends IntegrationBaseTest {

    @Autowired
    public IntegrationRevokeTokenTest(TestRestTemplate restTemplate,
                         CompanyRepository companyRepo,
                         CompanyUrlDataRepository companyUrlDataRepo, 
                         TopLevelDomainRepository topLevelDomainRepo,
                         UserRepository userRepo, 
                         UrlEncodingRepository urlEncodingRepo, 
                         TokenRepository tokenRepo,  
                         TokenUserLinkRepository tokenUserLinkRepo, 
                         CustomGenerator gen,
                         TokenController tokenController) {
        super(restTemplate, companyRepo, companyUrlDataRepo, topLevelDomainRepo, 
              userRepo, urlEncodingRepo, tokenRepo, tokenUserLinkRepo, gen, tokenController);
    }

    @SuppressWarnings("null")
    @Test
    public void testUnauthorizedUserRevokeToken() {
        for (int i = 0; i < 10; i++) {
            // Setup company and unauthorized user (no token)
            Company company = setUpCompany("TIER_1");
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), false);
            
            // Create auth headers
            HttpHeaders headers = createAuthHeaders(user);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            
            // Execute request and verify response
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=someuser@example.com",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response status is FORBIDDEN (403) for unauthorized user
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), 
                    "User with no token should get FORBIDDEN status");
            
            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("Their access might have been revoked"),
                    "Error message should indicate missing token");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
            assertEquals(companyCountBefore, companyRepo.count(), "Company count should not change");
        }
    }

    @Test
    public void testRevokeTokenLowerPriority() {
        for (int i = 0; i < 10; i++) {
            // Setup company and users with different roles
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser admin2 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser employee1 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            AppUser employee2 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            // Test invalid role combinations
            testInvalidRoleCombinationForRevoke(admin1, owner.getEmail());
            testInvalidRoleCombinationForRevoke(admin1, admin2.getEmail());
            testInvalidRoleCombinationForRevoke(employee1, owner.getEmail());
            testInvalidRoleCombinationForRevoke(employee1, admin1.getEmail());
            testInvalidRoleCombinationForRevoke(employee1, employee2.getEmail());
            
            // Test valid combinations
            testValidRoleCombinationForRevoke(owner, admin1.getEmail());
            testValidRoleCombinationForRevoke(owner, employee1.getEmail());
            testValidRoleCombinationForRevoke(admin1, employee1.getEmail());
        }
    }
    
    @SuppressWarnings("null")
    private void testInvalidRoleCombinationForRevoke(AppUser user, String targetEmail) {
        // Capture database state before
        long tokenCountBefore = tokenRepo.count();
        long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
        
        // Create auth headers
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Execute request and verify response
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/token/revoke?userEmail=" + targetEmail,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify response status is BAD_REQUEST (400) for invalid role combination
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), 
                "Should return BAD_REQUEST for invalid role combination");
        
        // Verify error message
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Cannot revoke token for role with equal or higher priority"),
                "Error message should indicate insufficient role authority");
        
        // Verify no change in database state
        assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
        assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
    }
    
    private void testValidRoleCombinationForRevoke(AppUser user, String targetEmail) {
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=" + targetEmail,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            assertEquals(HttpStatus.OK, response.getStatusCode(), 
                    "Valid role combination should return OK status");
        } catch (Exception e) {
            // Other exceptions are possible (like user not found) but not the role authority exception
            assertFalse(e.getMessage().contains("Cannot revoke token for role with equal or higher priority"),
                    "Error should not be about insufficient role authority");
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testRevokeTokenNonExistingUser() {
        for (int i = 0; i < 10; i++) {
            // Setup company and owner
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            
            String nonExistingEmail = "nonexistent" + i + "@example.com";
            
            // Create auth headers
            HttpHeaders headers = createAuthHeaders(owner);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            // Execute request and verify response
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=" + nonExistingEmail,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response status is BAD_REQUEST (400) for non-existing user
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), 
                    "Should return BAD_REQUEST for non-existing user");
            
            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("User not found"),
                    "Error message should indicate user not found");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testRevokeTokenExistingUserDifferentCompany() {
        for (int i = 0; i < 10; i++) {
            // Setup first company and owner
            Company company1 = setUpCompany("TIER_1");
            AppUser owner1 = setUpUser(company1, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            
            // Setup second company and user
            Company company2 = setUpCompany("TIER_1");
            AppUser user2 = setUpUser(company2, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            
            // Create auth headers for owner1
            HttpHeaders headers = createAuthHeaders(owner1);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            // Try to revoke token of user from different company
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=" + user2.getEmail(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response status is BAD_REQUEST (400)
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), 
                    "Should return BAD_REQUEST for user from different company");
            
            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("No user working for company"),
                    "Error message should indicate user not found in company");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
        }
    }
    
    @SuppressWarnings("null")
    @Test
    public void testRevokeTokenWithInvalidTokenUserLink() {
        for (int i = 0; i < 10; i++) {
            // Setup company, owner and a user without token link
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser userWithoutTokenLink = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
            // Create auth headers for owner
            HttpHeaders headers = createAuthHeaders(owner);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            
            // Try to revoke token of user without token link
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=" + userWithoutTokenLink.getEmail(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response status is BAD_REQUEST (400)
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), 
                    "Should return BAD_REQUEST for user without token link");
            
            // Verify error message
            assertTrue(Objects.requireNonNull(response.getBody()).contains("No token link found for user"),
                    "Error message should indicate no token link found");
            
            // Verify no change in database state
            assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
            assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
        }
    }
    
    @Test
    public void testSuccessfulRevokeToken() {
        for (int i = 0; i < 10; i++) {
            // Setup company with owner and employee with token
            Company company = setUpCompany("TIER_1");
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser employee = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            // Verify employee has token and link before revocation
            List<TokenUserLink> employeeLinks = tokenUserLinkRepo.findByUser(employee);
            assertFalse(employeeLinks.isEmpty(), "Employee should have token links before revocation");
            
            // Create auth headers for owner
            HttpHeaders headers = createAuthHeaders(owner);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Capture database state before
            long tokenCountBefore = tokenRepo.count();
            long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
            long companyCountBefore = companyRepo.count();
            
            // Revoke employee's token
            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/token/revoke?userEmail=" + employee.getEmail(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Verify response
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Successful revocation should return 200 OK");
            
            // Verify token state changes
            assertEquals(tokenCountBefore - 1, tokenRepo.count(), "Token count should decrease by 1");
            assertEquals(tokenUserLinkCountBefore - 1, tokenUserLinkRepo.count(), "TokenUserLink count should decrease by 1");
            
            // Verify employee no longer has token links
            List<TokenUserLink> linksAfterRevoke = tokenUserLinkRepo.findByUser(employee);
            assertTrue(linksAfterRevoke.isEmpty(), "Employee should have no token links after revocation");
            
            // Verify other state is unchanged
            assertEquals(companyCountBefore, companyRepo.count(), "Company count should not change");
        }
    }
}



@SpringBootTest(classes = TokenApiIntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationGetAllTokensTest extends IntegrationBaseTest {

	@Autowired
	public IntegrationGetAllTokensTest(TestRestTemplate restTemplate,
									   CompanyRepository companyRepo,
									   CompanyUrlDataRepository companyUrlDataRepo,
									   TopLevelDomainRepository topLevelDomainRepo,
									   UserRepository userRepo,
									   UrlEncodingRepository urlEncodingRepo,
									   TokenRepository tokenRepo,
									   TokenUserLinkRepository tokenUserLinkRepo,
									   CustomGenerator gen,
									   TokenController tokenController) {

		super(restTemplate, companyRepo, companyUrlDataRepo, topLevelDomainRepo, userRepo, urlEncodingRepo, tokenRepo, tokenUserLinkRepo, gen, tokenController);
	}

	private void testUserWithNoTokenGivenRole(Role role) {
		// Setup company and unauthorized user (no token)
		Company company = setUpCompany("TIER_1");
		AppUser user = setUpUser(company, role, false);
		UserDetails userDetails = new UserDetailsImp(user);

		// Capture database state before
		long tokenCountBefore = tokenRepo.count();
		long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
		long companyCountBefore = companyRepo.count();
		long cUrlDataBefore = companyUrlDataRepo.count();

		String roleString = role == null ? null : role.role();

		// Execute and verify
		Exception exception = assertThrows(
				TokenAuthController.TokenNotFoundException.class,
				() -> tokenController.getAllTokens(roleString, userDetails),
				"Should throw TokenNotFoundException for user with no token"
		);

		assertTrue(exception.getMessage().contains("Their access might have been revoked."),
				"Exception message should indicate missing token");

		// Verify no change in database state
		assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
		assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
		assertEquals(companyCountBefore, companyRepo.count(), "The company count should not change");
		assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "The company URL data should not change");

	}

	@Test
	public void testUserWithNoToken() {
		for (int i = 0; i < 10; i++) {
			testUserWithNoTokenGivenRole(null);
		}
		for (Role r : RoleManager.ROLES) {
			testUserWithNoTokenGivenRole(r);
		}
	}

	private void testValidRoleCombinationForGetToken(AppUser user, String role) {
		UserDetails userDetails = new UserDetailsImp(user);
		try {
			tokenController.getAllTokens(role, userDetails);
		} catch (TokenExceptions.InsufficientRoleAuthority e) {
			fail("Valid role combination should not throw InsufficientRoleAuthority: " + e.getMessage());
		} catch (Exception e) {
			// Other exceptions are possible (like user not found) but not the role authority exception
		}
	}

	private void testInvalidRoleCombinationForGetToken(AppUser user, String roleName) {
		UserDetails userDetails = new UserDetailsImp(user);

		// Capture database state before
		long tokenCountBefore = tokenRepo.count();
		long tokenUserLinkCountBefore = tokenUserLinkRepo.count();
		long companyCountBefore = companyRepo.count();
		long cUrlDataBefore = companyUrlDataRepo.count();

		// Execute and verify
		Exception exception = assertThrows(
				TokenExceptions.InsufficientRoleAuthority.class,
				() -> tokenController.getAllTokens(roleName, userDetails),
				"Should throw InsufficientRoleAuthority when retrieving tokens for equal or higher role"
		);

		assertTrue(exception.getMessage().contains("Cannot request tokens of users with higher priority"),
				"Exception message should indicate insufficient authority");

		// Verify no change in database state
		assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
		assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
		assertEquals(companyCountBefore, companyRepo.count(), "Company count should not change");
		assertEquals(cUrlDataBefore, companyUrlDataRepo.count(), "CompanyUrlData count should not change");
	}


	@Test
	public void testGetTokenWithHigherPriorityRole() {
		for (int i = 0; i < 10; i++) {
			// Setup company and users with different roles
			Company company = setUpCompany("TIER_1");
			AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);

			AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
			setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);

			AppUser employee1 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			// Test invalid role combinations
			testInvalidRoleCombinationForGetToken(admin1, RoleManager.OWNER_ROLE);
			testInvalidRoleCombinationForGetToken(admin1, RoleManager.ADMIN_ROLE);
			testInvalidRoleCombinationForGetToken(employee1, RoleManager.OWNER_ROLE);
			testInvalidRoleCombinationForGetToken(employee1, RoleManager.ADMIN_ROLE);
			testInvalidRoleCombinationForGetToken(employee1, RoleManager.EMPLOYEE_ROLE);


			testValidRoleCombinationForGetToken(owner, RoleManager.ADMIN_ROLE);
			testValidRoleCombinationForGetToken(owner, RoleManager.EMPLOYEE_ROLE);
			testValidRoleCombinationForGetToken(admin1, RoleManager.EMPLOYEE_ROLE);
		}
	}


	@Test
	public void testSuccessfulGetTokenWithRole() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		// add a time formatter to the object mapper
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		objectMapper.setDateFormat(df);

		for (int i = 0; i < 10; i++) {
			// Setup company with owner, 2 admins, 3 employees
			Company company = setUpCompany("TIER_1");

			AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
			Thread.sleep(500);

			AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
			Thread.sleep(500);

			setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
			Thread.sleep(500);

			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			Thread.sleep(500);

			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			Thread.sleep(500);

			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			UserDetails ownerDetails = new UserDetailsImp(owner);
			UserDetails adminDetails = new UserDetailsImp(admin1);

			// Capture database state before
			long tokenCountBefore = tokenRepo.count();
			long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

			// Owner retrieves admin tokens
			ResponseEntity<String> ownerResponse = tokenController.getAllTokens(RoleManager.ADMIN_ROLE, ownerDetails);
			assertEquals(HttpStatus.OK, ownerResponse.getStatusCode(), "Response status should be 200 OK");

			// Parse response and verify
			List<Map<String, Object>> adminTokensStrings = objectMapper.readValue(
					ownerResponse.getBody(),
					new TypeReference<>() {
					}
			);

			assertEquals(2, adminTokensStrings.size(), "Should return exactly 2 admin tokens");

			// Verify tokens are for admin role
			for (Map<String, Object> token : adminTokensStrings) {
				assertEquals(RoleManager.ADMIN_ROLE.toLowerCase(), token.get("role").toString().toLowerCase(), "Token should be for ADMIN role");
			}

			// extract the createAt timestamps from the tokens
			List<LocalDateTime> cDates1 = adminTokensStrings.stream()
					.map(m -> LocalDateTime.parse(m.get("createdAt").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toList();

			// make sure it is sorted
			assertEquals(cDates1.stream().sorted(Comparator.reverseOrder()).toList(), cDates1);


			// Admin retrieves employee tokens
			ResponseEntity<String> adminResponse = tokenController.getAllTokens(RoleManager.EMPLOYEE_ROLE, adminDetails);
			assertEquals(HttpStatus.OK, adminResponse.getStatusCode(), "Response status should be 200 OK");

			// Parse response and verify
			List<Map<String, Object>> employeeTokens = objectMapper.readValue(
					adminResponse.getBody(),
					new TypeReference<>() {
					}
			);

			assertEquals(3, employeeTokens.size(), "Should return exactly 3 employee tokens");

			// Verify tokens are for employee role
			for (Map<String, Object> token : employeeTokens) {
				assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), token.get("role").toString().toLowerCase(), "Token should be for EMPLOYEE role");
			}

			// extract the createAt timestamps from the tokens
			List<LocalDateTime> cDates2 = employeeTokens.stream()
					.map(m -> LocalDateTime.parse(m.get("createdAt").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toList();

			// make sure it is sorted
			assertEquals(cDates2.stream().sorted(Comparator.reverseOrder()).toList(), cDates2);

			// Verify database state is unchanged
			assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
			assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
		}
	}

	@Test
	public void testSuccessfulGetTokenWithNoRole() throws Exception {
		for (int i = 0; i < 10; i++) {
			// Setup company with owner, 2 admins, 3 employees
			Company company = setUpCompany("TIER_1");

			// owner
			AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
			// 2 admins
			AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
			Thread.sleep(1000);
			setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);


			// 3 employees
			AppUser employee1 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			Thread.sleep(1000);
			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			Thread.sleep(1000);
			setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			// user details objects
			UserDetails ownerDetails = new UserDetailsImp(owner);
			UserDetails adminDetails = new UserDetailsImp(admin1);
			UserDetails employeeDetails = new UserDetailsImp(employee1);

			// Capture database state before
			long tokenCountBefore = tokenRepo.count();
			long tokenUserLinkCountBefore = tokenUserLinkRepo.count();

			// 1. Owner retrieves all tokens (null role parameter)
			ResponseEntity<String> ownerResponse = tokenController.getAllTokens(null, ownerDetails);
			assertEquals(HttpStatus.OK, ownerResponse.getStatusCode(), "Response status should be 200 OK");

			// Parse response and verify
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			List<Map<String, Object>> allTokens = objectMapper.readValue(
					ownerResponse.getBody(),
					new TypeReference<>() {
					}
			);

			assertEquals(5, allTokens.size(), "Should return 5 tokens (2 admin + 3 employee)");

			// Verify first 2 tokens are admin tokens and next 3 are employee tokens
			assertEquals(RoleManager.ADMIN_ROLE.toLowerCase(), allTokens.get(0).get("role").toString().toLowerCase(), "First token should be ADMIN role");
			assertEquals(RoleManager.ADMIN_ROLE.toLowerCase(), allTokens.get(1).get("role").toString().toLowerCase(), "Second token should be ADMIN role");
			assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), allTokens.get(2).get("role").toString().toLowerCase(), "Third token should be EMPLOYEE role");
			assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), allTokens.get(3).get("role").toString().toLowerCase(), "Fourth token should be EMPLOYEE role");
			assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), allTokens.get(4).get("role").toString().toLowerCase(), "Fifth token should be EMPLOYEE role");


			List<LocalDateTime> adminTimeStamps = Stream.of(allTokens.get(0).get("createdAt"), allTokens.get(1).get("createdAt")).map(
							m -> LocalDateTime.parse(m.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
					.toList();

			assertEquals(adminTimeStamps.stream().sorted(Comparator.reverseOrder()).toList(), adminTimeStamps);

			List<LocalDateTime> employeeTimeStamps = Stream.of(allTokens.get(2).get("createdAt"), allTokens.get(3).get("createdAt"), allTokens.get(4).get("createdAt")).map(
							m -> LocalDateTime.parse(m.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
					.toList();

			assertEquals(employeeTimeStamps.stream().sorted(Comparator.reverseOrder()).toList(), employeeTimeStamps);


			// 2. Admin retrieves tokens (null role parameter)
			ResponseEntity<String> adminResponse = tokenController.getAllTokens(null, adminDetails);
			assertEquals(HttpStatus.OK, adminResponse.getStatusCode(), "Response status should be 200 OK");

			// Parse response and verify
			List<Map<String, Object>> adminViewTokens = objectMapper.readValue(
					adminResponse.getBody(),
					new TypeReference<>() {
					}
			);

			assertEquals(3, adminViewTokens.size(), "Admin should see 3 employee tokens");

			// Verify all tokens are employee tokens
			for (Map<String, Object> token : adminViewTokens) {
				assertEquals(RoleManager.EMPLOYEE_ROLE.toLowerCase(), token.get("role").toString().toLowerCase(), "Token should be for EMPLOYEE role");
			}

			List<LocalDateTime> adminViewTimeStamps = Stream.of(adminViewTokens.get(0).get("createdAt"), adminViewTokens.get(1).get("createdAt"), adminViewTokens.get(2).get("createdAt")).map(
							m -> LocalDateTime.parse(m.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
					.toList();

			assertEquals(adminViewTimeStamps.stream().sorted(Comparator.reverseOrder()).toList(), adminViewTimeStamps);


			// 3. Employee attempts to retrieve tokens (should throw exception)
			Exception exception = assertThrows(
					TokenExceptions.InsufficientRoleAuthority.class,
					() -> tokenController.getAllTokens(null, employeeDetails),
					"Employee should not be able to retrieve tokens with null role parameter"
			);

			assertTrue(exception.getMessage().contains("The role of the current user has no priority over any other role"),
					"Exception message should indicate insufficient authority");

			// Verify database state is unchanged
			assertEquals(tokenCountBefore, tokenRepo.count(), "Token count should not change");
			assertEquals(tokenUserLinkCountBefore, tokenUserLinkRepo.count(), "TokenUserLink count should not change");
		}
	}

}