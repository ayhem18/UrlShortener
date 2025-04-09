package org.companyManagerApi.tests;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.CompanyUrlDataRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.companyManagerApi.configurations.CmApiWebConfig;
import org.companyManagerApi.controllers.CompanyController;
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
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = CmApiWebConfig.class)
@WebMvcTest(CompanyController.class)
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
        Subscription sub= SubscriptionManager.getSubscription(subscriptionName);


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
    public void testUnauthenticatedUser(String endpoint, Map<String, String> paramsMap) throws Exception {
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
}



class ViewCompanyEndpointTests extends WebLayerBaseTest {
    @Autowired
    public ViewCompanyEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        testUnauthenticatedUser("/api/company/view", null);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany("TIER_1");

            for (String role : RoleManager.ROLES_STRING) {
                if (role.equalsIgnoreCase(RoleManager.EMPLOYEE_ROLE)) {
                    continue;
                }

                // Create user with authorized=true
                AppUser user = setUpUser(
                        company,
                        RoleManager.getRole(role),
                        true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/company/view");

                req = req.with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(
                                req
                        )
                        // neither 403 nor 401
                        .andExpect(status().is(not(403)))
                        .andExpect(status().is(not(403)))

                        .andReturn().getResponse().getContentAsString();
            }
        }
    }

    @Test
    void testNonSuccessfulAuth() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany("TIER_1");
            // Create user with authorized=true
            AppUser user = setUpUser(
                company,
                RoleManager.getRole(RoleManager.EMPLOYEE_ROLE),
                true
            );


            MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/company/view");

            req = req.with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

            // Make request with authentication
            mockMvc.perform(
                    req
                    )
                    .andExpect(status().is(403))
                    .andReturn().getResponse().getContentAsString();
        }
    }   
}

class UpdateSubscriptionEndpointTests extends WebLayerBaseTest {
    @Autowired
    public UpdateSubscriptionEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        Map<String, String> params = Map.of("subscription", "TIER_1");
        testUnauthenticatedUser("/api/company/subscription/update", params);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("FREE");
            
            // Only OWNER has CAN_WORK_WITH_SUBSCRIPTION authority
            AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(RoleManager.OWNER_ROLE),
                    true
            );

            MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .get("/api/company/subscription/update")
                .param("subscription", "TIER_1")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

            // Make request with authentication
            mockMvc.perform(req)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
        }
    }

    @Test
    void testNonSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("FREE");
            
            // Create ADMIN and EMPLOYEE users - both shouldn't have access
            for (String role : List.of(RoleManager.ADMIN_ROLE, RoleManager.EMPLOYEE_ROLE)) {
                AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(role),
                    true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                    .get("/api/company/subscription/update")
                    .param("subscription", "TIER_1")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                        .andExpect(status().is(403));
            }
        }
    }   
}

class ViewSubscriptionEndpointTests extends WebLayerBaseTest {
    @Autowired
    public ViewSubscriptionEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        testUnauthenticatedUser("/api/company/subscription/view", null);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // OWNER and EMPLOYEE have CAN_VIEW_SUBSCRIPTION authority
            for (String role : RoleManager.ROLES_STRING) {
                AppUser user = setUpUser(
                        company,
                        RoleManager.getRole(role),
                        true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                    .get("/api/company/subscription/view")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                        .andExpect(status().is(not(401)))
                        .andExpect(status().is(not(403)));
            }
        }
    }
}

class UpdateDomainEndpointTests extends WebLayerBaseTest {
    @Autowired
    public UpdateDomainEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        Map<String, String> params = Map.of(
            "newDomain", "newDomain.example.com",
            "deprecate", "false"
        );
        testUnauthenticatedUser("/api/company/domain/update", params);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // Only OWNER has CAN_UPDATE_DOMAIN_NAME authority
            AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(RoleManager.OWNER_ROLE),
                    true
            );

            MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .put("/api/company/domain/update")
                .param("newDomain", "newdDomain-" + i + ".example.com")
                .param("deprecate", "false")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

            // Make request with authentication
            mockMvc.perform(req)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
        }
    }

    @Test
    void testNonSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // Create ADMIN and EMPLOYEE users - both shouldn't have access
            for (String role : List.of(RoleManager.ADMIN_ROLE, RoleManager.EMPLOYEE_ROLE)) {
                AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(role),
                    true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                    .put("/api/company/domain/update")
                    .param("newDomain", "newdomain-" + i + ".example.com")
                    .param("deprecate", "false")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                        .andExpect(status().is(403));
            }
        }
    }   
}

class ViewUserEndpointTests extends WebLayerBaseTest {
    @Autowired
    public ViewUserEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        testUnauthenticatedUser("/api/company/users/view", null);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // Set up users of different roles
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser admin = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser employee = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            // Owner can view any user
            MockHttpServletRequestBuilder ownerReq = MockMvcRequestBuilders
                .get("/api/company/users/view")
                .param("userEmail", employee.getEmail())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(owner.getEmail(), owner.getPassword()));
            
            mockMvc.perform(ownerReq)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
                    
            // Admin can view employee (lower priority)
            MockHttpServletRequestBuilder adminReq = MockMvcRequestBuilders
                .get("/api/company/users/view")
                .param("userEmail", employee.getEmail())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(admin.getEmail(), admin.getPassword()));
            
            mockMvc.perform(adminReq)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
                    
            // Anyone can view themselves without userEmail param
            MockHttpServletRequestBuilder selfViewReq = MockMvcRequestBuilders
                .get("/api/company/users/view")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(employee.getEmail(), employee.getPassword()));
            
            mockMvc.perform(selfViewReq)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
        }
    }
}

class DeleteCompanyEndpointTests extends WebLayerBaseTest {
    @Autowired
    public DeleteCompanyEndpointTests(
        MockMvc mockMvc,
        CustomGenerator customGenerator,
        CompanyRepository companyRepo,
        UserRepository userRepo,
        TokenRepository tokenRepo,
        TokenUserLinkRepository tokenUserLinkRepo,
        TopLevelDomainRepository topLevelDomainRepo,
        CompanyUrlDataRepository companyUrlDataRepo
    ) {
        super(mockMvc, customGenerator, companyRepo, userRepo, tokenRepo, tokenUserLinkRepo, topLevelDomainRepo, companyUrlDataRepo);    
    }
    
    @Test   
    void testUnauthenticatedUser() throws Exception {
        testUnauthenticatedUser("/api/company/delete", null);
    }

    @Test
    void testSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // Only OWNER has CAN_DELETE_COMPANY authority
            AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(RoleManager.OWNER_ROLE),
                    true
            );

            MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                .delete("/api/company/delete")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

            // Make request with authentication
            mockMvc.perform(req)
                    .andExpect(status().is(not(401)))
                    .andExpect(status().is(not(403)));
        }
    }

    @Test
    void testNonSuccessfulAuth() throws Exception {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany("TIER_1");
            
            // Create ADMIN and EMPLOYEE users - both shouldn't have access
            for (String role : List.of(RoleManager.ADMIN_ROLE, RoleManager.EMPLOYEE_ROLE)) {
                AppUser user = setUpUser(
                    company,
                    RoleManager.getRole(role),
                    true
                );

                MockHttpServletRequestBuilder req = MockMvcRequestBuilders
                    .delete("/api/company/delete")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

                // Make request with authentication
                mockMvc.perform(req)
                        .andExpect(status().is(403));
            }
        }
    }   
}


