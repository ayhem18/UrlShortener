package org.authApi.tests;

import org.access.RoleManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.authApi.configurations.IntegrationTestConfig;
import org.company.entities.CompanyUrlData;
import org.company.repositories.CompanyUrlDataRepository;
import org.apiUtils.repositories.CounterRepository;
import org.authApi.requests.CompanyRegisterRequest;
import org.authApi.requests.CompanyVerifyRequest;
import org.authApi.requests.UserRegisterRequest;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.company.repositories.CompanyRepository;
import org.company.repositories.TopLevelDomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    private final TestRestTemplate restTemplate;
    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final TokenRepository tokenRepo;
    private final TokenUserLinkRepository tokenUserLinkRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final CompanyUrlDataRepository companyUrlDataRepo;
    private final CounterRepository counterRepo;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    // Test data
    private static final String COMPANY_ID = "test_company";
    private static final String COMPANY_NAME = "Test Company";
    private static final String EMAIL_DOMAIN = "testCompany.com";
    private static final String TLD = "www.test1company.com";
    private static final String OWNER_EMAIL = "owner@testCompany.com";
    private static final String ADMIN_EMAIL = "admin@testCompany.com";
    private static final String EMPLOYEE_EMAIL = "employee@testCompany.com";
    private static final String DEFAULT_PASSWORD = "password123";

    @Autowired
    public IntegrationTest(
            TestRestTemplate restTemplate,
            CompanyRepository companyRepo,
            UserRepository userRepo,
            TokenRepository tokenRepo,
            TokenUserLinkRepository tokenUserLinkRepo,
            TopLevelDomainRepository topLevelDomainRepo,
            CompanyUrlDataRepository companyUrlDataRepo,
            CounterRepository counterRepo,
            PasswordEncoder passwordEncoder) {
        
        this.restTemplate = restTemplate;
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.tokenUserLinkRepo = tokenUserLinkRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.companyUrlDataRepo = companyUrlDataRepo;
        this.counterRepo = counterRepo;
        this.passwordEncoder = passwordEncoder;
        
        // Configure ObjectMapper
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setDateFormat(df);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void setUp() {
        // Clear all data before each test
        tokenUserLinkRepo.deleteAll();
        tokenRepo.deleteAll();
        userRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        companyRepo.deleteAll();
        counterRepo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Ensure cleanup after each test
        tokenUserLinkRepo.deleteAll();
        tokenRepo.deleteAll();
        userRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        companyRepo.deleteAll();
        counterRepo.deleteAll();
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Test
    void testCompleteAuthenticationFlow() throws Exception {
        // Step 1: Register a new company
        CompanyRegisterRequest companyRequest = new CompanyRegisterRequest(
            COMPANY_ID,
            COMPANY_NAME,
            "123 Test Street",
            TLD,
            OWNER_EMAIL,
            EMAIL_DOMAIN,
            "TIER_1"
        );

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CompanyRegisterRequest> companyRequestEntity = new HttpEntity<>(companyRequest, headers);

        // Call API to register company
        ResponseEntity<String> companyResponse = restTemplate.postForEntity(
            "/api/auth/register/company",
            companyRequestEntity, 
            String.class
        );
        
        // Verify successful response
        assertEquals(HttpStatus.CREATED, companyResponse.getStatusCode());
        
        Map<String, Object> companyFields = objectMapper.readValue(companyResponse.getBody(), Map.class);
        
        // Verify company details
        assertEquals(COMPANY_ID, companyFields.get("id"));
        assertEquals(COMPANY_NAME, companyFields.get("companyName"));
        assertEquals(OWNER_EMAIL, companyFields.get("ownerEmail"));
        assertFalse((boolean) companyFields.get("verified"));

        // Verify top-level domain was created
        Optional<TopLevelDomain> domainOpt = topLevelDomainRepo.findByDomain(TLD);
        assertTrue(domainOpt.isPresent());
        assertEquals(COMPANY_ID, domainOpt.get().getCompany().getId());

        // Verify company URL data was created
        Optional<CompanyUrlData> urlDataOpt = companyUrlDataRepo.findFirstByCompany(companyRepo.findById(COMPANY_ID).get());
        assertTrue(urlDataOpt.isPresent());

        Company company = companyRepo.findById(COMPANY_ID).get();
        // Step 2: Find the owner token that was created
        List<AppToken> ownerTokens = tokenRepo.findByCompanyAndRole(
                company,
            RoleManager.getRole(RoleManager.OWNER_ROLE)
        );

        assertEquals(1, ownerTokens.size());
        AppToken ownerToken = ownerTokens.getFirst();
        assertEquals(AppToken.TokenState.INACTIVE, ownerToken.getTokenState());

        // Modify the token hash for testing (simulating what would happen via email)
        String testTokenValue = "test-owner-token-123";
        Field tokenHashField = AppToken.class.getDeclaredField("tokenHash");
        tokenHashField.setAccessible(true);
        tokenHashField.set(ownerToken, passwordEncoder.encode(testTokenValue));
        tokenRepo.save(ownerToken);

        // Step 3: Register the owner user
        UserRegisterRequest ownerUserRequest = new UserRegisterRequest(
            OWNER_EMAIL,
            "testOwner",
            DEFAULT_PASSWORD,
            "John",
            "Owner",
            "Smith",
            COMPANY_ID,
            "owner",
            null  // Owner doesn't need a token
        );

        ResponseEntity<String> ownerUserResponse = restTemplate.postForEntity(
            "/api/auth/register/user",
            ownerUserRequest, 
            String.class
        );

        assertEquals(HttpStatus.CREATED, ownerUserResponse.getStatusCode());

        Map<String, Object> ownerUserFields = objectMapper.readValue(ownerUserResponse.getBody(), Map.class);

        assertEquals(OWNER_EMAIL, ownerUserFields.get("email"));
        assertEquals("testOwner", ownerUserFields.get("username"));


        // Verify owner user was created
        Optional<AppUser> ownerUserOpt = userRepo.findById(OWNER_EMAIL);
        assertTrue(ownerUserOpt.isPresent());
        assertEquals("testOwner", ownerUserOpt.get().getUsername());

        // Step 4: Verify the company
        CompanyVerifyRequest verifyRequest = new CompanyVerifyRequest(
            COMPANY_ID,
            testTokenValue,
            OWNER_EMAIL
        );

        ResponseEntity<String> verifyResponse = restTemplate.postForEntity(
            "/api/auth/register/company/verify",
            verifyRequest,
            String.class
        );

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());

        // Verify company is now verified
        Optional<Company> verifiedCompanyOpt = companyRepo.findById(COMPANY_ID);
        assertTrue(verifiedCompanyOpt.isPresent());
        assertTrue(verifiedCompanyOpt.get().getVerified());

        // Verify owner token is now active
        Optional<AppToken> updatedTokenOpt = tokenRepo.findById(ownerToken.getTokenId());
        assertTrue(updatedTokenOpt.isPresent());
        assertEquals(AppToken.TokenState.ACTIVE, updatedTokenOpt.get().getTokenState());

        // Step 5: Create an admin token
        AppToken adminToken = new AppToken(
            "admin-token-123",
            passwordEncoder.encode("admin-token-value"),
            company,
            RoleManager.getRole(RoleManager.ADMIN_ROLE)
        );
        tokenRepo.save(adminToken);

        // Step 6: Register an admin user
        UserRegisterRequest adminUserRequest = new UserRegisterRequest(
            ADMIN_EMAIL,
            "testadmin",
            DEFAULT_PASSWORD,
            "Jane",
            "Admin",
            "Doe",
            COMPANY_ID,
            "admin",
            "admin-token-value"
        );   

        ResponseEntity<String> adminUserResponse = restTemplate.postForEntity(
            "/api/auth/register/user",
            adminUserRequest,
            String.class
        );

        assertEquals(HttpStatus.CREATED, adminUserResponse.getStatusCode());

        Map<String, Object> adminUserFields = objectMapper.readValue(adminUserResponse.getBody(), Map.class);

        assertEquals(ADMIN_EMAIL, adminUserFields.get("email"));

        // Verify admin user was created
        Optional<AppUser> adminUserOpt = userRepo.findById(ADMIN_EMAIL);
        assertTrue(adminUserOpt.isPresent());
        assertEquals("testadmin", adminUserOpt.get().getUsername());

        // Verify token is linked to admin user
        List<TokenUserLink> adminLinks = tokenUserLinkRepo.findByToken(adminToken);
        assertEquals(1, adminLinks.size());
        assertEquals(ADMIN_EMAIL, adminLinks.getFirst().getUser().getEmail());

        // Step 7: Create an employee token
        AppToken employeeToken = new AppToken(
            "employee-token-123",    
            passwordEncoder.encode("employee-token-value"),
            company,
            RoleManager.getRole(RoleManager.EMPLOYEE_ROLE)
        );
        tokenRepo.save(employeeToken);

        // Step 8: Register an employee user
        UserRegisterRequest employeeUserRequest = new UserRegisterRequest(
            EMPLOYEE_EMAIL,
            "testemployee",
            DEFAULT_PASSWORD,
            "Bob",
            "lastname",
            null,
            COMPANY_ID,
            "employee",
            "employee-token-value"
        );

        ResponseEntity<String> employeeUserResponse = restTemplate.postForEntity(
            "/api/auth/register/user",
            employeeUserRequest,
            String.class
        );

        assertEquals(HttpStatus.CREATED, employeeUserResponse.getStatusCode());

        Map<String, Object> employeeUserFields = objectMapper.readValue(employeeUserResponse.getBody(), Map.class);

        assertEquals(EMPLOYEE_EMAIL, employeeUserFields.get("email"));


        // Verify employee user was created
        Optional<AppUser> employeeUserOpt = userRepo.findById(EMPLOYEE_EMAIL);
        assertTrue(employeeUserOpt.isPresent());
        assertEquals("testemployee", employeeUserOpt.get().getUsername());

        // Step 9: Test negative scenarios - try registering user with already used token
        UserRegisterRequest duplicateTokenRequest = new UserRegisterRequest(
            "another@testcompany.com",
            "anotheruser",
            DEFAULT_PASSWORD,
            "Another",
            "Test",
            "User",
            COMPANY_ID,
            "employee",
            "employee-token-value"  // This token is already used
        );

        ResponseEntity<String> duplicateTokenResponse = restTemplate.postForEntity(
            "/api/auth/register/user",
            duplicateTokenRequest,
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, duplicateTokenResponse.getStatusCode());

        // Verify statistics and counts
        assertEquals(1, companyRepo.count());
        assertEquals(1, topLevelDomainRepo.count());
        assertEquals(3, userRepo.count()); // Owner, Admin, Employee
        assertEquals(3, tokenRepo.count()); // Owner token, Admin token, Employee token
        assertEquals(3, tokenUserLinkRepo.count()); // One link for each user-token pair
    }
} 