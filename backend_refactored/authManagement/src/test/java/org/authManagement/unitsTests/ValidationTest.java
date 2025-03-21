package org.authManagement.unitsTests;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomErrorMessage;
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
    private final ObjectMapper om;

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

        // set the object mapper to serialize LocalTimeDate objects
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.om = new ObjectMapper();
        this.om.setDateFormat(df);
        this.om.registerModule(new JavaTimeModule());
        this.om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

    @SuppressWarnings("unchecked")
    @Test
    void testRegisterCompanyValidateCompanyId() throws Exception {
        // create a short company id
        for (int i = 0; i < 10; i++) {
            double random = Math.random();

            int n;
            if (random < 0.5) {
                n = 1 + (new Random()).nextInt(6);
            } else {
                n = (new Random()).nextInt(10) + 17;
            }

            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    this.customGenerator.randomAlphaString(n),
                    "www."+this.customGenerator.randomAlphaString(n) + ".com",
                    this.customGenerator.randomAlphaString(n), // company name
                    this.customGenerator.randomAlphaString(n), // company address
                    "TIER_1",
                    "owner@example.com",
                    "example.com"
            );


            MvcResult res = mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/auth/register/company")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(this.om.writeValueAsString(request)
                                    )
                    )
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();

            CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                    CustomErrorMessage.class);

            Map<String, String> errorMap = new ObjectMapper().readValue(
                    errorResponse.getMessage(), Map.class
            );

            assertEquals("The length of id is expected to be between 8 and 16", errorMap.get("id"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMissingFieldsCompanyRegisterRequest() throws Exception{
        
        for (int i = 0; i < 50; i++) {
            // Determine which field to omit 
            int fieldToOmit = i % 6; 
            
            String companyId = fieldToOmit == 0 ? null : "company_" + i;
            String topLevelDomain = fieldToOmit == 1 ? null : "www.example" + i + ".com";
            String subscription = fieldToOmit == 2 ? null : "TIER_1";
            String ownerEmail = fieldToOmit == 3 ? null : "owner" + i + "@example" + i + ".com";
            String companyName = fieldToOmit == 4 ? null : "Company " + i;
            String companyAddress = fieldToOmit == 5 ? null : "Company Address " + i;


            // Create request with one missing field
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                companyId,
                companyName,
                companyAddress,
                topLevelDomain,
                ownerEmail,
                topLevelDomain != null ? topLevelDomain.substring(topLevelDomain.indexOf('.') + 1) : null,
                subscription
            );
            
            // Call API and verify result
            MvcResult res = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))

            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();

            CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                    CustomErrorMessage.class);

            Map<String, String> errorMap = new ObjectMapper().readValue(
                    errorResponse.getMessage(), Map.class
            );

            // determine which field to check depending on the fieldToOmit
            String fieldToCheck = switch (fieldToOmit) {
                case 0 -> "id";
                case 1 -> "topLevelDomain";
                case 2 -> "subscription";
                case 3 -> "ownerEmail";
                case 4 -> "companyName";
                case 5 -> "companyAddress";
                default -> "id";
            };  

            assertEquals(errorMap.get(fieldToCheck), "must not be blank");
        }

        // make sure that passing an empty mailDomain raises an error

        CompanyRegisterRequest request = new CompanyRegisterRequest(
            "company_1",
            "Company 1",
            "Company Address 1",
            "example.com",
            "owner@example.com",
            "",
            "TIER_1"
        );

        MvcResult res = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andReturn();

        CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                CustomErrorMessage.class);

        Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(), Map.class
        );

        assertEquals(errorMap.get("mailDomain"), "must not be empty");

    }
    
    @SuppressWarnings("unchecked")
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
                "companyName_" + i,
                "companyAddress_" + i,
                "www." + invalidDomain,
                "owner" + i + "@example.com",
                "example.com",
                "TIER_1"
                );
            
            // Call API and verify result
            MvcResult res = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))

            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

            CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                    CustomErrorMessage.class);

            Map<String, String> errorMap = new ObjectMapper().readValue(
                    errorResponse.getMessage(), Map.class
            );

            assertEquals(errorMap.get("topLevelDomain"), "the domain is expected to start with www. and end with at least a 2 character top level domain e.g ('org', 'edu', 'eu'...)");
        }
    }

    @SuppressWarnings("unchecked")
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

        int i = 0;
        // 2. Iterate through each provider
        for (String provider : standardProviders) {
            i += 1;
            // Create a company request using a standard provider as domain
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    "company_" + i,
                    "companyName_" + i,
                    "companyAddress_" + i,
                    "www.some_domain.com",
                    "owner@" + provider,                   // Owner email matching domain
                    provider,                               // Mail domain matching top level domain
                    "TIER_1"                              // Valid subscription tier
            );
            
            // Call API and verify result
            MvcResult res = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

            CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                    CustomErrorMessage.class);

            Map<String, String> errorMap = new ObjectMapper().readValue(
                    errorResponse.getMessage(), Map.class
            );

            assertEquals(errorMap.get("ownerEmail"), "Please provide a valid company email. Standard email providers (gmail, yahoo, outlook, etc.) are not accepted regardless of TLD");
        }
    }   

    @Test
    void testSuccessfulCompanyRegistration() throws Exception {
        // Test 50 successful company registrations
        for (int i = 0; i < 50; i++) {
            // Create unique company ID (ensure length is between 8-16 chars)
            String companyId = "company_" + String.format("%04d", i); // Results in company_0000 to company_0049
            
            // Create unique domain
            String domain = "www.example_" + i + ".com";
            
            // Create valid company registration request
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    "company_" + i,
                    "companyName_" + i,
                    "companyAddress_" + i,
                    "www.someDomain.com",
                    "owner@example.com" ,
                    "example.com",
                    "TIER_1"
            );

            // Call API to register company
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
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
            assertEquals(1, tokenRepo.findByCompany(savedCompany).size(), "exactly one token should be created for the company");

            // Verify token was created for the company (owner token)
            assertEquals(1, tokenRepo.findByCompanyAndRole(savedCompany, RoleManager.getRole(RoleManager.OWNER_ROLE)).size(), "exactly one token should be created for the company");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRegisterUserInvalidUsername() throws Exception {

        for (int i = 0; i < 50; i++) {

            String companyId = "company_" + String.format("%04d", i);

            // create a company
            CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
                companyId,
                "someCompanyName",
                "someCompanyAddress",
                "example.com",
                "TIER_1",
                "owner@example.com",
                null
            );

            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(this.om.writeValueAsString(companyRequest))
            );

            // generate a username that start with a non-alphabetic character
            char c = 'a';

            while (Character.isLetter(c)) {
                c = (char) (Math.random() * 128);
            }

            String username = c + customGenerator.randomAlphaString(10);

            UserRegisterRequest request = new UserRegisterRequest(
                "user" + c + "@example.com",
                username,
                "some_password",
                "User",
                "Test",
                "Middle",
                companyId,
                "owner",
                null
            );

            MvcResult res = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();

            CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                    CustomErrorMessage.class);

            Map<String, String> errorMap = new ObjectMapper().readValue(
                    errorResponse.getMessage(), Map.class
            );

            assertEquals(errorMap.get("username"), "Only alpha numerical characters are allowed + '_'. The first character must be alphabetic");
        }
    }

    @Test
    void testRegisterUserMissingFields() throws Exception {
        // Create a valid company first to use in the tests
        String companyId = "company_test";
        CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
            companyId,
            "Test Company",
            "123 Company Street",
            "example.com",
            "owner@example.com",
            "example.com",
            "TIER_1"
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(companyRequest))
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
            validEmail,
            validUsername,
            validPassword,
            "First",
            "Last",
            "Middle",
            "",
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(missingCompanyIdRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 2. Missing username
        UserRegisterRequest missingUsernameRequest = new UserRegisterRequest(
            validEmail,
            "",
            validPassword,
            "First",
            "Last",
            "Middle",
            companyId,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(missingUsernameRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 3. Missing password
        UserRegisterRequest missingPasswordRequest = new UserRegisterRequest(
            validEmail,
            validUsername,
            "",
            "First",
            "Last",
            "Middle",
            companyId,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(missingPasswordRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 4. Missing email
        UserRegisterRequest missingEmailRequest = new UserRegisterRequest(
            validEmail,
            validUsername,
            validPassword,
            "First",
            "Last",
            "Middle",
            companyId,
            validRole,
            validToken
        );
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(missingEmailRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        // 5. Missing role
        UserRegisterRequest missingRoleRequest = new UserRegisterRequest(
            validEmail,
            validUsername,
            validPassword,
            "First",
            "Last",
            "Middle",
            companyId,
            "",
            validToken
        );
        
        MvcResult res = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(missingRoleRequest))
        )
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andReturn();

        CustomErrorMessage errorResponse = this.om.readValue(res.getResponse().getContentAsString(),
                CustomErrorMessage.class);

        assertEquals(errorResponse.getMessage(), "Invalid role");
    }    
}






