package org.authManagement.tests;

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
import org.apiUtils.repositories.CounterRepository;
import org.authManagement.controllers.AuthController;
import org.authManagement.requests.CompanyRegisterRequest;
import org.authManagement.requests.CompanyVerifyRequest;
import org.authManagement.requests.UserRegisterRequest;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.CompanyUrlDataRepository;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomErrorMessage;
import org.utils.CustomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = WebTestConfig.class)
@WebMvcTest(AuthController.class)
public class WebLayerUnitTest {

    private final MockMvc mockMvc;
    private final CustomGenerator customGenerator;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final ObjectMapper om;
    private final CompanyUrlDataRepository companyUrlDataRepo;
    private final CounterRepository counterRepo;

    @Autowired
    public WebLayerUnitTest(
            MockMvc mockMvc,
            CustomGenerator customGenerator,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo,
            CounterRepository counterRepo) {
        this.mockMvc = mockMvc;
        this.customGenerator = customGenerator;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.companyUrlDataRepo = companyUrlDataRepo;
        this.counterRepo = counterRepo;

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
        tokenUserLinkRepo.deleteAll(); 
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulCompanyRegistration() throws Exception {
        // Clear repositories for clean test state
        userRepo.deleteAll();
        companyRepo.deleteAll();
        tokenRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        counterRepo.deleteAll();
        
        // Test 100 successful company registrations (increased from 50)
        for (int i = 0; i < 100; i++) {
            // Create unique company ID (ensure length is between 8-16 chars)
            String companyId = "company_" + String.format("%04d", i); // Results in company_0000 to company_0099
            
            // Create unique domain with more randomization
            String topLevelDomain = "www.exam" + i + "ple" + customGenerator.randomAlphaString(3) + ".com";
            
            // Create unique company name to avoid conflicts
            String uniqueCompanyName = "Test Company " + i + "-" + customGenerator.randomAlphaString(5);
            
            // Create valid company registration request
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    companyId,
                    uniqueCompanyName,                 // Unique company name
                    "Company Address " + i,           // Unique address
                    topLevelDomain,
                    "owner" + i + "@example.com",

                    "example.com",
                    "TIER_1"
            );

            // Call API to register company
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isCreated()); // Should return 201 Created

            // 1. Verify company was created in repository
            assertTrue(companyRepo.existsById(companyId), 
                      "Company with ID " + companyId + " should exist in repository");
            
            // 2. Retrieve and validate company data
            Optional<Company> savedCompanyOpt = companyRepo.findById(companyId);
            assertTrue(savedCompanyOpt.isPresent(), "Company should be retrievable from repository");
            
            Company savedCompany = savedCompanyOpt.get();
            assertEquals(companyId, savedCompany.getId(), "Company ID should match request");
            assertEquals(uniqueCompanyName, savedCompany.getCompanyName(), "Company name should match request");
            assertEquals(request.ownerEmail(), savedCompany.getOwnerEmail(), "Owner email should match request");
            assertEquals(request.mailDomain(), savedCompany.getEmailDomain(), "Email domain should match request");
            assertEquals(SubscriptionManager.getSubscription("TIER_1").toString(), 
                        savedCompany.getSubscription().toString(), "Subscription should match request");
            assertFalse(savedCompany.getVerified(), "Newly created company should not be verified yet");
            
            // 3. Verify top level domain was created
            Optional<TopLevelDomain> savedDomainOpt = topLevelDomainRepo.findByDomain(topLevelDomain);
            assertTrue(savedDomainOpt.isPresent(), "Top level domain should be created");
            assertEquals(companyId, savedDomainOpt.get().getCompany().getId(), 
                        "Domain should be associated with correct company");
            
            // 4. Verify token was created for the company (owner token)
            assertEquals(1, tokenRepo.findByCompany(savedCompany).size(), 
                        "Exactly one token should be created for the company");
            assertEquals(1, tokenRepo.findByCompanyAndRole(
                        savedCompany, 
                        RoleManager.getRole(RoleManager.OWNER_ROLE)).size(), 
                        "Exactly one owner token should be created for the company");
            
            // 5. Verify the counterRepo was updated with the correct count
            assertEquals(i + 1, counterRepo.findByCollectionName(Company.COMPANY_COLLECTION_NAME).get().getCount(), 
                        "Counter should track the number of companies created");
            
            // 6. Verify that CompanyUrlData was created with correct hash
            Optional<CompanyUrlData> urlData = companyUrlDataRepo.findFirstByCompany(savedCompany);
            assertTrue(urlData.isPresent(), "CompanyUrlData should exist for the company");
            assertEquals(savedCompany.getId(), urlData.get().getCompany().getId(), 
                        "CompanyUrlData should reference the correct company");
            
            // 7. Verify the hash was generated correctly
            String expectedHash = customGenerator.generateId(
                    counterRepo.findByCollectionName(Company.COMPANY_COLLECTION_NAME).get().getCount() - 1 
                    + AuthController.companySiteHashOffset);
            assertEquals(expectedHash, urlData.get().getCompanyDomainHashed(),
                        "Company site hash should be generated with the correct formula");
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

    @SuppressWarnings({"unchecked"})
    @Test
    void testRegisterUserMissingFields() throws Exception {
        // Create a valid company first to use in the tests
        String companyId = "company_test";
        CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
            companyId,
            "Test Company",
            "123 Company Street",
            "www.example.com",
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

        // Valid base request parameters
        String validUsername = "validUser123";
        String validPassword = "password123";
        String validEmail = "user@example.com";
        String validFirstName = "John";
        String validLastName = "Doe";
        String validMiddleName = "Robert";
        String validRole = "employee";
        String validToken = "some_token";

        // 1. Define all possible error messages for each field
        Map<String, List<String>> fieldErrorMessages = Map.of(
            "email", List.of(
                "must not be blank", 
                "Please provide a valid email address"
            ),
            "username", List.of(
                "must not be blank", 
                "username must of length between 2 and 16", 
                "Only alpha numerical characters are allowed + '_'. The first character must be alphabetic"
            ),
            "password", List.of(
                "must not be blank",
                "password must of length between 8 and 16"
            ),
            "firstName", List.of(
                "must not be blank"
            ),
            "lastName", List.of(
                "must not be blank"
            ),
            "companyId", List.of(
                "must not be blank",
                "The length of id is expected to be between 8 and 16"
            ),
            "role", List.of(
                "must not be blank"
            ),
            "middleName", List.of(
                "can be null but not empty"
            ),
            "roleToken", List.of(
                "can be null but not empty"
            )
        );
        
        // 2. Testing fields with @NotBlank annotation (can't be null or empty string)
        List<String> notBlankFields = List.of(
            "email",
            "username",
            "password",
            "firstName",
            "lastName",
            "companyId",
            "role"
        );
        
        // Test each @NotBlank field with empty string
        for (String missingValue: List.of("", "null")) {
        
        for (String field : notBlankFields) {
            UserRegisterRequest request = switch (field) {
                case "email" -> new UserRegisterRequest(
                        missingValue.equals("null") ? null : missingValue,
                        validUsername,
                        validPassword,
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        companyId,
                        validRole,
                        validToken
                );
                case "username" -> new UserRegisterRequest(
                        validEmail,
                        missingValue.equals("null") ? null : missingValue,                  // Empty username
                        validPassword,
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        companyId,
                        validRole,
                        validToken
                );
                case "password" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        missingValue.equals("null") ? null : missingValue,                  // Empty password
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        companyId,
                        validRole,
                        validToken
                );
                case "firstName" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        missingValue.equals("null") ? null : missingValue,                  // Empty firstName
                        validLastName,
                        validMiddleName,
                        companyId,
                        validRole,
                        validToken
                );
                case "lastName" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        validFirstName,
                        missingValue.equals("null") ? null : missingValue,                  // Empty lastName
                        validMiddleName,
                        companyId,
                        validRole,
                        validToken
                );
                case "companyId" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        missingValue.equals("null") ? null : missingValue,                  // Empty companyId
                        validRole,
                        validToken
                );
                case "role" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        companyId,
                        missingValue.equals("null") ? null : missingValue,                  // Empty role
                        validToken
                );
                default ->
                    // Should never reach here
                        throw new IllegalStateException("Unexpected field: " + field);
            };
            
            // Perform request and verify response
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Verify the error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(), 
                Map.class
            );
            
            // Get the actual error message
            String actualErrorMessage = errorMap.get(field);
            
            // Get possible error messages for this field
            List<String> possibleErrorMessages = fieldErrorMessages.get(field);
            
            assertNotNull(actualErrorMessage, 
                "Error message for " + field + " should be present");
            
            // Check if actual error message matches any of the possible messages
            boolean matchesAnyExpectedMessage = possibleErrorMessages.stream()
                .anyMatch(actualErrorMessage::contains);
            
            assertTrue(matchesAnyExpectedMessage, 
                "Error message for " + field + " should match one of the expected messages. Actual: " + 
                actualErrorMessage + ", Expected one of: " + possibleErrorMessages);
            }
        }

        // 3. Testing fields with @NotEmpty annotation (can be non-null but empty string)        
        List<String> notEmptyFields = List.of("middleName", "roleToken");

        // Test each @NotEmpty field with empty string
        for (String field : notEmptyFields) {
            UserRegisterRequest request = switch (field) {
                case "middleName" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        validFirstName,
                        validLastName,
                        "",                  // Empty middleName
                        companyId,
                        validRole,
                        validToken
                );
                case "roleToken" -> new UserRegisterRequest(
                        validEmail,
                        validUsername,
                        validPassword,
                        validFirstName,
                        validLastName,
                        validMiddleName,
                        companyId,
                        validRole,
                        ""                   // Empty roleToken
                );
                default ->
                    // Should never reach here
                        throw new IllegalStateException("Unexpected field: " + field);
            };

            // Perform request and verify response
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Verify the error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(), 
                Map.class
            );
            
            // Get the actual error message
            String actualErrorMessage = errorMap.get(field);
            
            // Get possible error messages for this field
            List<String> possibleErrorMessages = fieldErrorMessages.get(field);
            
            assertNotNull(actualErrorMessage, 
                "Error message for " + field + " should be present");
            
            // Check if actual error message matches any of the possible messages
            boolean matchesAnyExpectedMessage = possibleErrorMessages.stream()
                .anyMatch(actualErrorMessage::contains);
            
            assertTrue(matchesAnyExpectedMessage, 
                "Error message for " + field + " should match one of the expected messages. Actual: " + 
                actualErrorMessage + ", Expected one of: " + possibleErrorMessages);
        }
        
    }    

    @SuppressWarnings("unchecked")
    @Test
    void testRegisterUserWithInvalidEmail() throws Exception {
        // Create a valid company first to use in the tests
        String companyId = "company_test";
        CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
            companyId,
            "Test Company",
            "123 Company Street",
            "www.example.com",
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

        // Valid parameters for user registration
        String validUsername = "validUser123";
        String validPassword = "password123";
        String validFirstName = "John";
        String validLastName = "Doe";
        String validMiddleName = "Robert";
        String validRole = "employee";
        String validToken = "some_token";
        
        // List of invalid email formats to test
        List<String> invalidEmails = List.of(
            "invalid-email",           // Missing @ symbol
            "invalid@",                // Missing domain
            "@missing-local.com",      // Missing local part
            "inv alid@domain.com",     // Space in local part
            "invalid@domain@com",      // Multiple @ symbols
            "invalid@.com",            // Missing domain name
            "invalid@domain.",         // Domain ending with dot
            "invalid@domain..com",     // Consecutive dots
            ".invalid@domain.com",     // Starting with dot
            "invalid..email@domain.com" // Consecutive dots in local part
        );
        
        for (String invalidEmail : invalidEmails) {
            UserRegisterRequest request = new UserRegisterRequest(
                invalidEmail,
                validUsername,
                validPassword,
                validFirstName,
                validLastName,
                validMiddleName,
                companyId,
                validRole,
                validToken
            );
            
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Extract error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(),
                Map.class
            );
            
            // Verify email field has an error
            assertNotNull(errorMap.get("email"), 
                "Error message for email should be present for invalid format: " + invalidEmail);
            
            // Verify the error message mentions email validation
            String errorMessage = errorMap.get("email");
            assertTrue(
                errorMessage.contains("Please provide a valid email address"),
                "Error message should contain 'Please provide a valid email address' but was: " + errorMessage + 
                    " for invalid email: " + invalidEmail
                );
            }
        }

    @SuppressWarnings("unchecked")
    @Test
    void testVerifyCompanyValidateId() throws Exception {
        // Test with IDs of invalid lengths
        List<String> invalidIds = List.of(
            "short",                       // Too short (less than 8 chars)
            "way_too_long_company_id_123"  // Too long (more than 16 chars)
        );
        
        for (String invalidId : invalidIds) {
            CompanyVerifyRequest request = new CompanyVerifyRequest(
                invalidId,
                "valid-token-123",
                "owner@example.com"
            );
            
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Extract error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(),
                Map.class
            );
            
            // Verify companyId field has an error
            assertNotNull(errorMap.get("companyId"), 
                "Error message for companyId should be present for invalid ID: " + invalidId);
            
            // Verify the error message mentions length constraints
            String errorMessage = errorMap.get("companyId");
            assertTrue(
                errorMessage.contains("The length of id is expected to be between 8 and 16"),
                "Error message should contain length constraints but was: " + errorMessage
            );
        }
        
        // Test with a valid ID (should pass validation)
        String validId = "company_12345";
        CompanyVerifyRequest validRequest = new CompanyVerifyRequest(
            validId,
            "valid-token-123",
            "owner@example.com"
        );
        
        // This might fail with other errors (like company not found), but not with validation errors
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/company/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(validRequest))
        )
        .andExpect(result -> assertFalse(
            result.getResolvedException() instanceof MethodArgumentNotValidException,
            "Request with valid ID should not fail ID validation"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testVerifyCompanyValidateEmail() throws Exception {
        // List of invalid email formats to test
        List<String> invalidEmails = List.of(
            "invalid-email",           // Missing @ symbol
            "invalid@",                // Missing domain
            "@missing-local.com",      // Missing local part
            "inv alid@domain.com",     // Space in local part
            "invalid@domain@com",      // Multiple @ symbols
            "invalid@.com",            // Missing domain name
            "invalid@domain.",         // Domain ending with dot
            "invalid@domain..com"      // Consecutive dots
        );
        
        for (String invalidEmail : invalidEmails) {
            CompanyVerifyRequest request = new CompanyVerifyRequest(
                "company_12345",       // Valid ID
                "valid-token-123",     // Valid token
                invalidEmail           // Invalid email
            );
            
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Extract error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(),
                Map.class
            );
            
            // Verify email field has an error
            assertNotNull(errorMap.get("email"), 
                "Error message for email should be present for invalid format: " + invalidEmail);
            
            // Verify the error message mentions email validation
            String errorMessage = errorMap.get("email");
            assertTrue(
                errorMessage.contains("Please provide a valid email address"),
                "Error message should indicate valid email is required but was: " + errorMessage + 
                " for invalid email: " + invalidEmail
            );
        }
        
        // Test with a valid email (should pass validation)
        String validEmail = "owner@example.com";
        CompanyVerifyRequest validRequest = new CompanyVerifyRequest(
            "company_12345",
            "valid-token-123",
            validEmail
        );
        
        // This might fail with other errors (like company not found), but not with email validation errors
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/register/company/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(validRequest))
        )
        .andExpect(result -> assertFalse(
            result.getResolvedException() instanceof MethodArgumentNotValidException,
            "Request with valid email should not fail email validation"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testVerifyCompanyMissingFields() throws Exception {
        // Define all required fields
        Map<String, List<String>> fieldErrorMessages = Map.of(
            "companyId", List.of("must not be blank", "The length of id is expected to be between 8 and 16"),
            "token", List.of("must not be blank"),
            "email", List.of("must not be blank", "Please provide a valid email address")
        );
        
        // Create requests with each field missing one at a time
        String validId = "company_12345";
        String validToken = "valid-token-123";
        String validEmail = "owner@example.com";
        
        Map<String, CompanyVerifyRequest> requestsWithMissingFields = Map.of(
            "companyId", new CompanyVerifyRequest("", validToken, validEmail),
            "token", new CompanyVerifyRequest(validId, "", validEmail),
            "email", new CompanyVerifyRequest(validId, validToken, "")
        );
        
        // Test each field with empty value
        for (Map.Entry<String, CompanyVerifyRequest> entry : requestsWithMissingFields.entrySet()) {
            String fieldName = entry.getKey();
            CompanyVerifyRequest request = entry.getValue();
            
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(request))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Extract error message
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(),
                Map.class
            );
            
            // Verify the field has an error
            assertNotNull(errorMap.get(fieldName), 
                "Error message for " + fieldName + " should be present when it's missing");
            
            // Get the actual error message
            String actualErrorMessage = errorMap.get(fieldName);
            
            // Get possible error messages for this field
            List<String> possibleErrorMessages = fieldErrorMessages.get(fieldName);
            
            // Check if actual error message matches any of the possible messages
            boolean matchesAnyExpectedMessage = possibleErrorMessages.stream()
                .anyMatch(actualErrorMessage::contains);
            
            assertTrue(matchesAnyExpectedMessage, 
                "Error message for " + fieldName + " should match one of the expected messages. Actual: " + 
                actualErrorMessage + ", Expected one of: " + possibleErrorMessages);
        }
        
        // Test with null values
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (String fieldName : fieldErrorMessages.keySet()) {
            
            CompanyVerifyRequest req = new CompanyVerifyRequest(fieldName.equals("companyId") ? null : validId,
                    fieldName.equals("token") ? null : validToken,
                    fieldName.equals("email") ? null : validEmail);
            
            MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register/company/verify") 
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req))
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andReturn();
            
            // Similar validation as above
            CustomErrorMessage errorResponse = om.readValue(
                result.getResponse().getContentAsString(),
                CustomErrorMessage.class
            );
            
            Map<String, String> errorMap = new ObjectMapper().readValue(
                errorResponse.getMessage(),
                Map.class
            );
            
            assertNotNull(errorMap.get(fieldName), 
                "Error message for " + fieldName + " should be present when it's null");
        }
    }

    @Test
    void testNullableFieldsInUserRegisterRequest() throws Exception {
        // Create a valid company first
    String companyId = "company_id_123";
    CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
        companyId,
        "Test Company",
        "123 Company Street",
        "www.example.com",
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
    
    // Valid base request parameters
    String validEmail = "user@example.com";
    String validUsername = "validUser123";
    String validPassword = "password123";
    String validFirstName = "John";
    String validLastName = "Doe";
    String validRole = "employee";
    String validToken = "some_token";
    
    // Test 1: Verify null middleName is allowed (should pass validation)
    UserRegisterRequest nullMiddleNameRequest = new UserRegisterRequest(
        validEmail, 
        validUsername,
        validPassword,
        validFirstName,
        validLastName,
        null,              // null middleName
        companyId,
        validRole,
        validToken
    );
    
    mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/register/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(nullMiddleNameRequest))
    )
    .andExpect(result -> assertFalse(
        result.getResolvedException() instanceof MethodArgumentNotValidException,
        "Request with null middleName should not fail validation"
    ));
    
    // Test 2: Verify empty middleName is rejected
    UserRegisterRequest emptyMiddleNameRequest = new UserRegisterRequest(
        validEmail, 
        validUsername,
        validPassword,
        validFirstName,
        validLastName,
        "",              // empty middleName
        companyId,
        validRole,
        validToken
    );
    
    mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/register/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(emptyMiddleNameRequest))
    )
    .andExpect(MockMvcResultMatchers.status().isBadRequest())
    .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException(), "Request with empty middleName should fail validation"));
    
    // Test 3: Verify null roleToken for owner role is allowed
    UserRegisterRequest ownerNullTokenRequest = new UserRegisterRequest(
        validEmail, 
        validUsername,
        validPassword,
        validFirstName,
        validLastName,
        validFirstName,    
        companyId,
        "owner",           
        null               
    );
    
    mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/register/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(ownerNullTokenRequest))
    )
    .andExpect(result -> assertFalse(
        result.getResolvedException() instanceof MethodArgumentNotValidException,
        "Request with null roleToken for owner role should not fail validation"
    ));
    
    // Test 4: Verify empty roleToken is rejected
    UserRegisterRequest emptyTokenRequest = new UserRegisterRequest(
        validEmail, 
        validUsername,
        validPassword,
        validFirstName,
        validLastName,
        validFirstName,    
        companyId,
        validRole,
        "" // empty role              
    );
    
    mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/register/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(emptyTokenRequest))
    )
    .andExpect(MockMvcResultMatchers.status().isBadRequest())
    .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException(), "Request with empty roleToken should fail validation"));
    }
}