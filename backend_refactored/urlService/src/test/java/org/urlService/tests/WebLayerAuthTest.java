package org.urlService.tests;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.access.Role;
import org.access.RoleManager;
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
import org.urlService.configurations.UrlServiceWebConfig;
import org.urlService.controllers.UrlController;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = UrlServiceWebConfig.class)
@WebMvcTest(UrlController.class)
public class WebLayerAuthTest {

    private final MockMvc mockMvc;
    private final CustomGenerator customGenerator;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final CompanyUrlDataRepository companyUrlDataRepo;
    private final PasswordEncoder encoder;
    private final ObjectMapper om;

    @Autowired
    public WebLayerAuthTest(
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

    }

    @BeforeEach
    void setUp() {
        // Reset the repositories before each test
        companyRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll();
    }


    // Helper method to set up a test company with domains
    private Company setUpCompany() {
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
            SubscriptionManager.getSubscription("TIER_1")
        );
        companyRepo.save(testCompany);

        // Create active domain
        String activeDomainName = "www." + companyName + "00active.com";
        TopLevelDomain activeDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            activeDomainName,
            encoder.encode(activeDomainName).replaceAll("/", "_"),
            testCompany
        );
        topLevelDomainRepo.save(activeDomain);
        
        // Create inactive domain
        String inactiveDomainName = "www." + companyName + "00inactive.com";
        TopLevelDomain inactiveDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            inactiveDomainName,
            encoder.encode(inactiveDomainName).replaceAll("/", "_"),
            testCompany
        );
        inactiveDomain.deactivate();
        topLevelDomainRepo.save(inactiveDomain);

        // Create deprecated domain
        String deprecatedDomainName = "www." + companyName + "00deprecated.com";
        TopLevelDomain deprecatedDomain = new TopLevelDomain(
            customGenerator.randomAlphaString(10),
            deprecatedDomainName,
            encoder.encode(deprecatedDomainName).replaceAll("/", "_"),
            testCompany
        );
        deprecatedDomain.deprecate();
        topLevelDomainRepo.save(deprecatedDomain);

        // Create company URL data
        CompanyUrlData companyUrlData = new CompanyUrlData(
            testCompany,
            this.encoder.encode(activeDomainName).replaceAll("/", "_")
        );
        companyUrlDataRepo.save(companyUrlData);
        return testCompany;
    }

    // Helper method to set up a test user
    private AppUser setUpUser(Company company, Role role, boolean authorized) {
        String username = "testuser_" + customGenerator.randomAlphaString(5);
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
        AppToken token = new AppToken(customGenerator.randomString(12), encoder.encode("tokenId"), company, role );
        token.activate();
        tokenRepo.save(token);

        if (authorized) {
            // create a token user link for the user
            TokenUserLink tokenUserLink = new TokenUserLink("link_id", token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        return userRepo.save(user);

    }


    /**
     * Test that requests with non-existing users return 401 Unauthorized
     */
    @Test
    public void testUnauthorizedAccess() throws Exception {
        // Use non-existent credentials
        mockMvc.perform(MockMvcRequestBuilders.get("/api/url/encode").param("url", "https://www.example.com"))
                .andExpect(status().isUnauthorized());
    }
    
    /**
     * Test that authenticated users with proper authorization can access the endpoint
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAuthorizedAccess() throws Exception {
        for (int i = 0; i < 20; i++) {
            Company company = setUpCompany();

            List<TopLevelDomain> domains = this.topLevelDomainRepo.findByCompany(company);

            assertFalse(domains.isEmpty(), "Company should have at least one active domain");

            String domain = domains.getFirst().getDomain();

            for (String role : RoleManager.ROLES_STRING) {
                Role r = RoleManager.getRole(role);

                // Create user with authorized=true
                AppUser user = setUpUser(
                    company, 
                    r, 
                    true
                );


                MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/url/encode").param("url", "https://" + domain + "/product/123")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));


                // Make request with authentication
                String result = mockMvc.perform(
                        req
                        )
                        .andExpect(status().is(not(401)))  // Not unauthorized
                        .andExpect(status().is(not(403)))  // Not forbidden
                        .andReturn().getResponse().getContentAsString();

                // read the json response as a map
                Map<String, String> responseMap = om.readValue(result, Map.class);

                // Additional verification - should have successful response
                assertTrue(responseMap.containsKey("encoded_url"), 
                        "Response should contain encoded URL for user " + user.getEmail());
                }
        }
    }
    
    /**
     * Test that authenticated users without proper authorization receive 403 Forbidden
     */
    @Test
    public void testAuthenticatedButUnauthorized() throws Exception {
        // Set up company and domain
        Company company = setUpCompany();
        List<TopLevelDomain> domains = this.topLevelDomainRepo.findByCompany(company);
        String domain = domains.getFirst().getDomain();
        
        // Set up user with authorized=false
        AppUser user = setUpUser(
            company, 
            RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), 
            false
        );
        
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/url/encode").param("url", "https://" + domain + "/product/123")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.getEmail(), user.getPassword()));

        // Make request with authentication but without authorization
        mockMvc.perform(req)
                .andExpect(status().isForbidden());
    }
}
