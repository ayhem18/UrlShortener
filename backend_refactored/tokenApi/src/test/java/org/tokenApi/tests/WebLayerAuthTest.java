package org.tokenApi.tests;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.tokenApi.configurations.TokenApiWebConfig;
import org.tokenApi.controllers.TokenController;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.access.SubscriptionManager;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.CompanyUrlDataRepository;
import org.company.repositories.TopLevelDomainRepository;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringJUnitConfig(classes = TokenApiWebConfig.class)
@WebMvcTest(TokenController.class)
class WebLayerBaseTest {
    protected final MockMvc mockMvc;
    protected final CustomGenerator customGenerator;
    protected final CompanyRepository companyRepo;
    protected final UserRepository userRepo;
    protected final TokenRepository tokenRepo;
    protected final TokenUserLinkRepository tokenUserLinkRepo;
    protected final TopLevelDomainRepository topLevelDomainRepo;
    protected final CompanyUrlDataRepository companyUrlDataRepo;
    protected final PasswordEncoder encoder;
    protected final ObjectMapper om;

    @Autowired
    public WebLayerBaseTest(
            MockMvc mockMvc,
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo) {
                
        this.mockMvc = mockMvc;
        this.customGenerator = customGenerator;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.companyUrlDataRepo = companyUrlDataRepo;
        this.encoder = new BCryptPasswordEncoder();

        // set the object mapper to serialize LocalTimeDate objects
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.om = new ObjectMapper();
        this.om.setDateFormat(df);
        this.om.registerModule(new JavaTimeModule());
        this.om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @BeforeEach
    @AfterEach
    void clear() {
        // Reset the repositories before each test
        companyRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll();
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

        String companyId = customGenerator.randomAlphaString(12);
        String companyName = customGenerator.randomAlphaString(10);
        String companyEmailDomain = customGenerator.randomAlphaString(5) + ".com";
        String companyEmail = "owner@" + companyEmailDomain;

        Company testCompany = new Company(
            companyId,
            companyName,
            customGenerator.randomAlphaString(10),
            companyEmail,
            companyEmailDomain,
            sub
        );
        companyRepo.save(testCompany);

        // Create active domain
        String activeDomainName = "www." + companyName + "00active.com";
        TopLevelDomain activeDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            activeDomainName,
            testCompany
        );
        topLevelDomainRepo.save(activeDomain);
        
        // Create inactive domain
        String inactiveDomainName = "www." + companyName + "00inactive.com";
        TopLevelDomain inactiveDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            inactiveDomainName,
            testCompany
        );
        inactiveDomain.deactivate();
        topLevelDomainRepo.save(inactiveDomain);

        // Create deprecated domain
        String deprecatedDomainName = "www." + companyName + "00deprecated.com";
        TopLevelDomain deprecatedDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            deprecatedDomainName,
            testCompany
        );
        deprecatedDomain.deprecate();
        topLevelDomainRepo.save(deprecatedDomain);

        // Create company URL data
        CompanyUrlData companyUrlData = new CompanyUrlData(
            customGenerator.randomAlphaString(20),
            testCompany,
            this.encoder.encode(activeDomainName).replaceAll("/", "_")
        );
        companyUrlDataRepo.save(companyUrlData);
        return testCompany;
    }

    // Helper method to set up a test user
    protected AppUser setUpUser(Company company, Role role, boolean authorized) {
        String username = "test_user_" + customGenerator.randomAlphaString(10);
        String email = username + "@" + company.getEmailDomain();
        
        String password = customGenerator.randomAlphaString(12);

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

        // save the use t
        userRepo.save(user);

        // create a token for the user
        AppToken token = new AppToken(customGenerator.randomString(12), encoder.encode("tokenId"), company, role );
        token.activate();
        tokenRepo.save(token);

        if (authorized) {
            // create a token user link for the user
            TokenUserLink tokenUserLink = new TokenUserLink("link_id_" + username, token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        user.setPassword(password);
        // return the use with the original password; this way the user in the database in encoded while we still have access to the original password for testing purposes.
        return user;
    }

    /**
     * Test that requests without credentials receive 401 Unauthorized
     */
    public void testUnauthorizedAccess(String endpoint, Map<String, String> paramsMap) throws Exception {
        // the server below will call the endpoint without any credentials
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(endpoint);

        if (paramsMap != null) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                req = req.param(entry.getKey(), entry.getValue());
            }
        }

        mockMvc.perform(req)
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test that authenticated users without proper token receive 403 Forbidden
     */
    public void testAuthenticatedButNoToken(String endpoint) throws Exception {
        for (int i = 0; i < 20; i++) {
            int companyIndex = 1 + i % 2;
            Company company = setUpCompany("test" + companyIndex);

            for (String role : RoleManager.ROLES_STRING) {
                // employees are not allowed to generate tokens
                if (role.equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
                    continue;
                }
    
                Role r = RoleManager.getRole(role);

                // Create user with authorized=true
                AppUser user = setUpUser(
                    company,
                    r,
                    false
                );

                // find roles with lower priority than the current role
                List<Role> lowerPriorityRoles = RoleManager.ROLES_STRING.stream()
                    .filter(r2 -> RoleManager.getRole(r2).getPriority() < r.getPriority())
                    .map(RoleManager::getRole).toList();

                for (Role lowerPriorityRole : lowerPriorityRoles) {
                    String lowerPriorityRoleString = lowerPriorityRole.role();

                    MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get(endpoint).param("role", lowerPriorityRoleString)
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));


                    // Make request with authentication
                    mockMvc.perform(
                            req
                            )
                            .andExpect(status().is(not(401)))  // Not unauthorized
                            .andExpect(status().is(not(403)))  // Not forbidden
                            .andReturn().getResponse().getContentAsString();        
                }
            }
        }
    }

}


@SpringJUnitConfig(classes = TokenApiWebConfig.class)
@WebMvcTest(TokenController.class)
class GenerateTokenAuthTest extends WebLayerBaseTest {

    @Autowired
    public GenerateTokenAuthTest(
            MockMvc mockMvc,
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);
    }

    /**
     * Test that requests without credentials receive 401 Unauthorized
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        for (String role : RoleManager.ROLES_STRING) {
            Map<String, String> params = Map.of("role", role);
            testUnauthorizedAccess("/api/token/generate", params);
        }
    }
    
    /**
     * Test that authenticated users without proper token receive 403 Forbidden
     */
    @Test
    public void testAuthenticatedButNoToken() throws Exception {
        testAuthenticatedButNoToken("/api/token/generate");
    }

    /**
     * Test that authenticated users with proper roles can access the endpoint (roles that have the CAN_WORK_WITH_TOKENS authority)
     */
    @Test
    public void testAuthorizedAccess() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany("test1");

            for (String role : RoleManager.ROLES_STRING) {
                // employees are not allowed to generate tokens
                if (role.equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
                    continue;
                }
    
                Role r = RoleManager.getRole(role);

                // Create user with authorized=true
                AppUser user = setUpUser(
                    company,
                    r,
                    true
                );

                // find roles with lower priority than the current role
                List<Role> lowerPriorityRoles = RoleManager.ROLES_STRING.stream()
                    .filter(r2 -> RoleManager.getRole(r2).getPriority() < r.getPriority())
                    .map(RoleManager::getRole).toList();

                for (Role lowerPriorityRole : lowerPriorityRoles) {
                    String lowerPriorityRoleString = lowerPriorityRole.role();

                    MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/token/generate").param("role", lowerPriorityRoleString)
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));


                    // Make request with authentication
                    mockMvc.perform(
                            req
                            )
                            .andExpect(status().is(not(401)))  // Not unauthorized
                            .andExpect(status().is(not(403)));  // Not forbidden
                }
            }
        }
    }
}

class RevokeTokenAuthTest extends WebLayerBaseTest {

    // autowired is necessary here to load the parameters from the context
    @Autowired
    public RevokeTokenAuthTest(
            MockMvc mockMvc,    
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);
    }


    /**
     * Test that requests with non-existing users return 401 Unauthorized
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        for (String role : RoleManager.ROLES_STRING) {
            Map<String, String> params = Map.of("role", role);
            testUnauthorizedAccess("/api/token/revoke", params);
        }
    }
    
    /**
     * Test that authenticated users without proper authorization receive 403 Forbidden
     */
    @Test
    public void testAuthenticatedButNoToken() throws Exception {
        testAuthenticatedButNoToken("/api/token/revoke");
    }


    /**
     * Test that authenticated users with proper authorization can access the revoke token endpoint
     */
    @Test
    public void testAuthorizedAccess() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany("test1");

            for (String role : RoleManager.ROLES_STRING) {
                if (role.equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
                    continue;
                }

                Role r = RoleManager.getRole(role);

                // Create user with authorized=true
                AppUser user = setUpUser(
                    company, 
                    r, 
                    true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/token/revoke")
                    .param("userEmail", user.getEmail())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                    .andExpect(status().is(not(401)))  // Not unauthorized
                    .andExpect(status().is(not(403))); // Not forbidden
            }
        }
    }
}

class GetAllTokensAuthTest extends WebLayerBaseTest {

    @Autowired
    public GetAllTokensAuthTest(
            MockMvc mockMvc,
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        for (String role : RoleManager.ROLES_STRING) {
            Map<String, String> params = Map.of("role", role);
            testUnauthorizedAccess("/api/token/get", params);
        }
    }

    /**
     * Test that authenticated users without proper token receive 403 Forbidden for decode endpoint
     */
    @Test
    public void testAuthenticatedButNoToken() throws Exception {
        testAuthenticatedButNoToken("/api/token/get");
    }

    

    /**
     * Test that authenticated users with proper roles can access the endpoint
     */
    @Test
    public void testAuthorizedAccess() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany("test1");
            
            for (String role : RoleManager.ROLES_STRING) {
                if (role.equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
                    continue;
                }

                Role r = RoleManager.getRole(role);

                // Create user with authorized=true
                AppUser user = setUpUser(
                    company, 
                    r, 
                    true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/token/get")
                    .param("role", role)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                    .andExpect(status().is(not(401)))  // Not unauthorized
                    .andExpect(status().is(not(403))); // Not forbidden
            }
        }
    }

}