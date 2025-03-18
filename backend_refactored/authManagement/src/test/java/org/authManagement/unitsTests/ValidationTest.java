package org.authManagement.unitsTests;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.access.RoleManager;
import org.access.SubscriptionManager;
import org.authManagement.configurations.WebTestConfig;
import org.authManagement.controllers.AuthController;
import org.authManagement.requests.CompanyRegisterRequest;
import org.authManagement.requests.UserRegisterRequest;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = WebTestConfig.class)
@WebMvcTest(AuthController.class)
public class ValidationTest {

    private final MockMvc mockMvc;
    private final CustomGenerator customGenerator;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ValidationTest(
            MockMvc mockMvc,
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo) {
        this.mockMvc = mockMvc;
        this.customGenerator = customGenerator;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
    }
    
    @BeforeEach
    void setUp() {
        // Clear all repositories to ensure a clean state before each test
        tokenUserLinkRepo.deleteAll();  // Clear links first due to references
        tokenRepo.deleteAll();
        userRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        companyRepo.deleteAll();
        
    }

    @Test
    void testRegisterCompanyValidateCompanyId() throws Exception {
        // create a short company id
        for (int i = 0; i < 10; i++) {
            double random = Math.random();

            int n;
            if (random < 0.5) {
                n = (new Random()).nextInt(7);
            } else {
                n = (new Random()).nextInt(10) + 17;
            }

            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    this.customGenerator.randomAlphaString(n),
                    this.customGenerator.randomAlphaString(n) + ".com",
                    "TIER_1",
                    "owner@example.com",
                    "example.com"
            );


            mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/auth/register/company")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(request)
                                    )
                    )
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    @Test
    void testMissingFieldsCompanyRegisterRequest() throws Exception{
        
        for (int i = 0; i < 50; i++) {
            // Determine which field to omit 
            int fieldToOmit = i % 4; 
            
            String companyId = fieldToOmit == 0 ? null : "company_" + i;
            String topLevelDomain = fieldToOmit == 1 ? null : "example" + i + ".com";
            String subscription = fieldToOmit == 2 ? null : "TIER_1";
            String ownerEmail = fieldToOmit == 3 ? null : "owner" + i + "@example" + i + ".com";
            
            // Create request with one missing field
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                companyId,
                topLevelDomain,
                subscription,
                ownerEmail,
                topLevelDomain != null ? topLevelDomain.substring(topLevelDomain.indexOf('.') + 1) : null
            );
            
            // Call API and verify result
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))

            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }
    
    @Test
    void testInvalidDomainCompanyRegisterRequest() throws Exception {
        // Test 50 different invalid domain scenarios
        for (int i = 0; i < 50; i++) {
            // Create an invalid domain based on iteration modulo 4
            String invalidDomain = switch (i % 4) {
                case 0 ->
                    // Domain without a dot
                        "invalidDomain" + i;
                case 1 ->
                    // Domain with invalid characters
                        "invalid@domain" + i + ".com";
                case 2 ->
                    // Domain with whitespace
                        "invalid domain" + i + ".com";
                case 3 ->
                    // Domain with TLD less than 2 characters
                        "invalidDomain" + i + ".a";
                default -> "invalid.com";
            };

            // Create valid request with invalid domain
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                "company_" + i,
                invalidDomain,
                "TIER_1",
                "owner" + i + "@example.com",
                "example.com"
            );
            
            // Call API and verify result
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))

            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    @Test
    void testStandardEmailProviderRejection() throws Exception {
        // 1. Create a list of standard email providers that should be rejected
        // This list should exactly match the providers in the CompanyRegisterRequest regex pattern
        List<String> standardProviders = List.of(
            "gmail.com",
            "yahoo.com",
            "yahoo.fr",
            "yahoo.com",
            "outlook.com",
            "hotmail.com",
            "aol.com",
            "protonmail.com",
            "icloud.com",
            "mail.com",
            "zoho.com",
            "yandex.com",
            "gmx.com",
            "live.com",
            "msn.com"
        );
        
        // 2. Iterate through each provider
        for (String provider : standardProviders) {
            // Create a company request using a standard provider as domain
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                "company_" + provider.replaceAll("\\.", ""), // Remove dots for valid ID
                provider,                              // Use provider as domain
                "TIER_1",                              // Valid subscription tier
                "owner@" + provider,                   // Owner email matching domain
                provider                               // Mail domain matching top level domain
            );
            
            // Call API and verify result
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }   

    @Test
    void testSuccessfulCompanyRegistration() throws Exception {
        // Test 50 successful company registrations
        for (int i = 0; i < 50; i++) {
            // Create unique company ID (ensure length is between 8-16 chars)
            String companyId = "company_" + String.format("%04d", i); // Results in company_0000 to company_0049
            
            // Create unique domain
            String domain = "example_" + i + ".com";
            
            // Create valid company registration request
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                companyId,
                domain,
                "TIER_1",
                "owner" + i + "@customdomain" + i + ".com", // Must not use standard provider
                null
            );
            
            // Call API to register company
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isCreated()) // Should return 201 Created
            .andReturn();
            
            // Verify company was created in repository
            assertTrue(companyRepo.existsById(companyId), 
                      "Company with ID " + companyId + " should exist in repository");
            
            // Retrieve and validate company data
            Optional<Company> savedCompanyOpt = companyRepo.findById(companyId);
            assertTrue(savedCompanyOpt.isPresent(), "Company should be retrievable from repository");
            
            Company savedCompany = savedCompanyOpt.get();
            assertEquals(companyId, savedCompany.getId(), "Company ID should match request");
            assertEquals(request.ownerEmail(), savedCompany.getOwnerEmail(), "Owner email should match request");
            assertEquals(request.mailDomain(), savedCompany.getEmailDomain(), "Email domain should match request");
            assertEquals(SubscriptionManager.getSubscription("TIER_1").toString(), 
                        savedCompany.getSubscription().toString(), "Subscription should match request");
            assertFalse(savedCompany.getVerified(), "Newly created company should not be verified yet");
            
            // Verify top level domain was created
            Optional<TopLevelDomain> savedDomainOpt = topLevelDomainRepo.findByDomain(domain);
            assertTrue(savedDomainOpt.isPresent(), "Top level domain should be created");
            assertEquals(companyId, savedDomainOpt.get().getCompany().getId(), 
                        "Domain should be associated with correct company");
            
            // Verify token was created for the company (owner token)
            assertEquals(1, tokenRepo.findByCompany(savedCompany).size(), "At least one token should be created for the company");

            // Verify token was created for the company (owner token)
            assertEquals(1, tokenRepo.findByCompanyAndRole(savedCompany, RoleManager.getRole(RoleManager.OWNER_ROLE)).size(), "At least one token should be created for the company");
        }
    }

    @Test
    void testRegisterUserInvalidUsername() throws Exception {

        for (int i = 0; i < 50; i++) {

            String companyId = "company_" + String.format("%04d", i);

            // create a company
            CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
                companyId,
                "example.com",
                "TIER_1",
                "owner@example.com",
                null
            );

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(companyRequest))
            );

            // generate a username that start with a non-alphabetic character
            char c = 'a';

            while (Character.isLetter(c)) {
                c = (char) (Math.random() * 128);
            }

            String username = c + customGenerator.randomAlphaString(10);

            UserRegisterRequest request = new UserRegisterRequest(
                companyId,
                username, 
                "some_password",
                "owner@example.com",
                "owner",
                null
            );

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        }
    }

    @Test
    void testRegisterUserMissingFields() throws Exception {
        // Create a valid company first to use in the tests
        String companyId = "company_test";
        CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
            companyId,
            "example.com",
            "TIER_1",
            "owner@example.com",
            "example.com"
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(companyRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isCreated());
        
        // Valid base request (will modify field by field)
        String validUsername = "validUser";
        String validPassword = "password123";
        String validEmail = "user@example.com";
        String validRole = "employee";
        String validToken = "some_token";
        
        // Test each field one by one
        // 1. Missing companyId
        UserRegisterRequest missingCompanyIdRequest = new UserRegisterRequest(
            "",  // Empty companyId
            validUsername,
            validPassword,
            validEmail,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingCompanyIdRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 2. Missing username
        UserRegisterRequest missingUsernameRequest = new UserRegisterRequest(
            companyId,
            "",  // Empty username
            validPassword,
            validEmail,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingUsernameRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 3. Missing password
        UserRegisterRequest missingPasswordRequest = new UserRegisterRequest(
            companyId,
            validUsername,
            "",  // Empty password
            validEmail,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingPasswordRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 4. Missing email
        UserRegisterRequest missingEmailRequest = new UserRegisterRequest(
            companyId,
            validUsername,
            validPassword,
            "",  // Empty email
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingEmailRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 5. Missing role
        UserRegisterRequest missingRoleRequest = new UserRegisterRequest(
            companyId,
            validUsername,
            validPassword,
            validEmail,
            "",  // Empty role
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missingRoleRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    
}






