package org.urlService.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.access.RoleManager;
import org.access.SubscriptionManager;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.urlService.configurations.IntegrationTestConfig;
import org.user.entities.AppUser;
import org.user.repositories.UrlEncodingRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationBaseTest {
    @Autowired
    protected TestRestTemplate restTemplate;
    
    @Autowired
    protected CompanyRepository companyRepo;
    
    @Autowired
    protected CompanyUrlDataRepository companyUrlDataRepo;
    
    @Autowired
    protected TopLevelDomainRepository topLevelDomainRepo;
    
    @Autowired
    protected UserRepository userRepo;
    
    @Autowired
    protected UrlEncodingRepository urlEncodingRepo;
    
    @Autowired
    protected TokenRepository tokenRepo;
    
    @Autowired
    protected TokenUserLinkRepository tokenUserLinkRepo;
    
    @Autowired
    protected CustomGenerator gen;
    
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final PasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @BeforeEach
    public void setUp() {
        clear();
    }
    
    @AfterEach
    public void tearDown() {
        clear();
    }
    
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
    
    protected Company setUpCompany() {
        String companyId = gen.randomAlphaString(10);
        String companyName = "Test Company " + gen.randomAlphaString(5);
        String companyAddress = "Address " + gen.randomAlphaString(5);
        String emailDomain = gen.randomAlphaString(5) + ".com";
        String ownerEmail = gen.randomAlphaString(5) + "@" + emailDomain;

        Company company = new Company(
                companyId, 
                companyName, 
                companyAddress, 
                ownerEmail,
                emailDomain,
                SubscriptionManager.getSubscription("TIER_1")
        );
        company.verify();
        companyRepo.save(company);

        String tld = "www." + this.gen.randomAlphaString(10) + ".com";
        // Create top-level domain
        TopLevelDomain topLevelDomain = new TopLevelDomain(
                gen.randomAlphaString(10),
                tld,
                company
        );
        topLevelDomainRepo.save(topLevelDomain);
        
        // Create company URL data
        CompanyUrlData companyUrlData = new CompanyUrlData(
                company,
                encoder.encode(tld)
        );
        companyUrlDataRepo.save(companyUrlData);
        
        return company;
    }
    
    protected AppUser setUpUser(Company company, String role, boolean authorized) {
        return setUpUser(company, RoleManager.getRole(role), authorized);
    }
    
    protected AppUser setUpUser(Company company, org.access.Role role, boolean authorized) {
        String email = gen.randomAlphaString(5) + "@" + company.getEmailDomain();
        String username = gen.randomAlphaString(8);
        String password = gen.randomAlphaString(10);
        String firstName = gen.randomAlphaString(5);
        String lastName = gen.randomAlphaString(5);
        
        AppUser user = new AppUser(
                email,
                username,
                encoder.encode(password), // Use encoded password for storage
                firstName,
                lastName,
                null,
                company,
                role
        );
        userRepo.save(user);
        
        if (authorized) {
            // Create token
            AppToken token = new AppToken(
                    gen.randomAlphaString(10),
                    gen.randomAlphaString(10),
                    company,
                    role
            );
            token.activate();
            tokenRepo.save(token);
            
            // Create token-user link
            TokenUserLink tokenUserLink = new TokenUserLink(
                    gen.randomAlphaString(10),
                    token,
                    user
            );
            tokenUserLinkRepo.save(tokenUserLink);
        }
        
        // Return user with original password (not encoded) for HTTP basic auth
        user.setPassword(password);
        return user;
    }
    
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


@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationUrlEncodeTest extends IntegrationBaseTest {
    
    @Test
    void testUserWithoutToken() {
        // Set up company
        Company company = setUpCompany();
        
        // Create user WITHOUT token link (authorized=false)
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, false);
        
        // Find active domain
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        // Request to encode URL
        String originalUrl = "https://" + activeDomain.getDomain() + "/test";
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/encode?url=" + originalUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify unauthorized
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    void testInvalidUrlException() {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Invalid URL (no protocol)
        String invalidUrl = "invalid-url";
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/encode?url=" + invalidUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void testSuccessfulEncode() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Find active domain
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        // Request to encode URL
        String originalUrl = "https://" + activeDomain.getDomain() + "/test";
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Capture state before encoding
        long urlCount = urlEncodingRepo.count();
        CompanyUrlData dataBefore = companyUrlDataRepo.findByCompany(company).get();
        int encodedDataSizeBefore = dataBefore.getDataEncoded().size();
        int decodedDataSizeBefore = dataBefore.getDataDecoded().size();
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/encode/?url=" + originalUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify success
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Parse response to get encoded URL
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        assertTrue(jsonNode.has("encoded_url"), "Response should contain encoded_url field");
        
        // Verify encoding creation
        assertEquals(urlCount + 1, urlEncodingRepo.count(), "URL encoding count should increase by 1");
        
        // Verify company URL data has been updated
        CompanyUrlData dataAfter = companyUrlDataRepo.findByCompany(company).get();
        assertTrue(dataAfter.getDataEncoded().size() >= encodedDataSizeBefore, "Encoded data maps should grow or stay the same");
        assertTrue(dataAfter.getDataDecoded().size() >= decodedDataSizeBefore, "Decoded data maps should grow or stay the same");
    }
}


@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationUrlDecodeTest extends IntegrationBaseTest {

    @Test
    void testUserWithoutToken() {
        // Set up company
        Company company = setUpCompany();
        
        // Create user WITHOUT token link (authorized=false)
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, false);
        
        // Find active domain
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        // Get company URL data
        CompanyUrlData urlData = companyUrlDataRepo.findByCompany(company).get();
        String domainHash = urlData.getCompanyDomainHashed();
        
        // Sample encoded URL
        String encodedUrl = "https://localhost:8080/" + domainHash + "/abc123";
        
        // Request to decode URL
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/decode/?encodedUrl=" + encodedUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify unauthorized
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    void testInvalidDomainHash() {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Invalid domain hash
        String invalidDomainHash = "invalidhash";
        String encodedUrl = "https://localhost:8080/" + invalidDomainHash + "/abc123";
        
        // Request to decode URL
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Capture state before decoding
        long urlCount = urlEncodingRepo.count();
        CompanyUrlData dataBefore = companyUrlDataRepo.findByCompany(company).get();
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/decode/?encodedUrl=" + encodedUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        // Verify state is unchanged
        assertEquals(urlCount, urlEncodingRepo.count(), "URL encoding count should not change");
        CompanyUrlData dataAfter = companyUrlDataRepo.findByCompany(company).get();
        assertEquals(dataBefore.getDataEncoded().size(), dataAfter.getDataEncoded().size(), 
                "Encoded data maps should not change");
        assertEquals(dataBefore.getDataDecoded().size(), dataAfter.getDataDecoded().size(), 
                "Decoded data maps should not change");
    }
    
    @Test
    void testSuccessfulEncodeDecode() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Find active domain
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        // First encode a URL
        String originalUrl = "https://" + activeDomain.getDomain() + "/test?param=value";
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> encodeResponse = restTemplate.exchange(
                "/api/url/encode/?url=" + originalUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertEquals(HttpStatus.OK, encodeResponse.getStatusCode());
        
        // Parse response to get encoded URL
        JsonNode encodeJson = objectMapper.readTree(encodeResponse.getBody());
        String encodedUrl = encodeJson.get("encoded_url").asText();
        
        // Capture state after encoding
        long urlCount = urlEncodingRepo.count();
        CompanyUrlData dataBefore = companyUrlDataRepo.findByCompany(company).get();
        
        // Now decode the URL
        ResponseEntity<String> decodeResponse = restTemplate.exchange(
                "/api/url/decode/?encodedUrl=" + encodedUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertEquals(HttpStatus.OK, decodeResponse.getStatusCode());
        
        // Parse response to get decoded URL
        JsonNode decodeJson = objectMapper.readTree(decodeResponse.getBody());
        String decodedUrl = decodeJson.get("decoded_url").asText();
        
        // Verify database state is unchanged after decoding
        assertEquals(urlCount, urlEncodingRepo.count(), "URL encoding count should not change after decode");
        
        // Verify company URL data hasn't changed
        CompanyUrlData dataAfter = companyUrlDataRepo.findByCompany(company).get();
        assertEquals(dataBefore.getDataDecoded().size(), dataAfter.getDataDecoded().size(), 
                "Data decoded size should not change after decode operation");
        assertEquals(dataBefore.getDataEncoded().size(), dataAfter.getDataEncoded().size(), 
                "Data encoded size should not change after decode operation");
        
        // Verify decoded URL matches original
        assertEquals(originalUrl, decodedUrl, "Decoded URL should match the original URL");
    }
}


@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationUrlHistoryTest extends IntegrationBaseTest {
    
    @Test
    void testUserWithoutToken() {
        // Set up company
        Company company = setUpCompany();
        
        // Create user WITHOUT token link (authorized=false)
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, false);
        
        // Request to get history
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/history/?page=0&size=10",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify unauthorized
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // Also test parameterless endpoint
        ResponseEntity<String> responseNoParams = restTemplate.exchange(
                "/api/url/history",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertEquals(HttpStatus.FORBIDDEN, responseNoParams.getStatusCode());
    }
    
    @Test
    void testHistorySizeLimit() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Get history size from subscription
        Integer historySize = company.getSubscription().getMaxHistorySize();
        assertNotNull(historySize, "History size should be defined in subscription");
        
        // Find active domain for URL creation
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Create and encode a series of URLs (more than the history size)
        int totalUrls = historySize + 5;
        for (int i = 0; i < totalUrls; i++) {
            String originalUrl = "https://" + activeDomain.getDomain() + "/product/" + i;
            
            // Encode URL
            restTemplate.exchange(
                    "/api/url/encode/?url=" + originalUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Test history size at each step
            ResponseEntity<String> historyResponse = restTemplate.exchange(
                    "/api/url/history/?page=0&size=" + totalUrls,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
            
            JsonNode jsonNode = objectMapper.readTree(historyResponse.getBody());
            
            // For values less than history size, response size should match the loop index
            // For values beyond history size, response size should be capped at history size
            int expectedSize = Math.min(i + 1, historySize);
            assertEquals(expectedSize, jsonNode.size(), 
                    "History size should be " + expectedSize + " after " + (i + 1) + " URLs");
        }
        
        // Test with parameterless endpoint too
        ResponseEntity<String> fullHistoryResponse = restTemplate.exchange(
                "/api/url/history",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertEquals(HttpStatus.OK, fullHistoryResponse.getStatusCode());
        
        JsonNode jsonNode = objectMapper.readTree(fullHistoryResponse.getBody());
        assertEquals(historySize, jsonNode.size(), 
                "Full history should respect subscription size limit");
    }
    
    @Test
    void testHistoryOrder() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.EMPLOYEE_ROLE, true);
        
        // Find active domain for URL creation
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        int historySize = company.getSubscription().getMaxHistorySize();
        
        // Create a list of URLs to encode
        List<String> originalUrls = new ArrayList<>();
        for (int i = 0; i < historySize; i++) {
            originalUrls.add("https://" + activeDomain.getDomain() + "/page/" + i);
        }
        
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Encode each URL in sequence and verify history order
        for (int i = 0; i < originalUrls.size(); i++) {
            String currentUrl = originalUrls.get(i);
            
            // Encode URL
            restTemplate.exchange(
                    "/api/url/encode/?url=" + currentUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Get history
            ResponseEntity<String> historyResponse = restTemplate.exchange(
                    "/api/url/history/?page=0&size=" + (i + 1),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
            
            JsonNode jsonNode = objectMapper.readTree(historyResponse.getBody());
            
            // Verify size matches expected
            int expectedSize = i + 1;
            assertEquals(expectedSize, jsonNode.size(), 
                    "History should have " + expectedSize + " entries");
            
            // Verify order (newest first)
            for (int j = 0; j < expectedSize; j++) {
                String expectedUrl = originalUrls.get(i - j); // Reverse order
                String actualUrl = jsonNode.get(j).get("url").asText();
                assertEquals(expectedUrl, actualUrl, 
                        "History entry at position " + j + " should match URL at " + (i - j));
            }
        }
        
        // Test with parameterless endpoint too
        ResponseEntity<String> fullHistoryResponse = restTemplate.exchange(
                "/api/url/history",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        assertEquals(HttpStatus.OK, fullHistoryResponse.getStatusCode());
        
        JsonNode fullJsonNode = objectMapper.readTree(fullHistoryResponse.getBody());
        
        // Verify full history has all entries in reverse order
        assertEquals(historySize, fullJsonNode.size(), "Full history should have correct size");
        
        for (int j = 0; j < historySize; j++) {
            String expectedUrl = originalUrls.get(originalUrls.size() - 1 - j); // Reverse order
            String actualUrl = fullJsonNode.get(j).get("url").asText();
            assertEquals(expectedUrl, actualUrl, 
                    "Full history entry at position " + j + " should match URL in reverse order");
        }
    }
}   