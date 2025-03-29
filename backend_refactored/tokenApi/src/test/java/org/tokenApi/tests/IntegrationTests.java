package org.tokenApi.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.url.UrlProcessor;
import org.tokenApi.configurations.IntegrationTestConfig;
import org.user.entities.AppUser;
import org.user.entities.UrlEncoding;
import org.user.repositories.UrlEncodingRepository;
import org.user.repositories.UserRepository;
import org.utils.CustomGenerator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class subForTest implements Subscription {

        @Override
        public Integer getMaxHistorySize() {
                return 3;
        }

        @Override
        public String getTier() {
                return "TEST_SUB";
        }

        @Override
        public Integer getMaxNumLevels() {
                return 5;
        }

        @Override
        public Integer getMaxAdmins() {
                return 1;
        }

        @Override
        public Integer getMaxEmployees() {
                return 1;
        }

        @Override
        public Integer getEncodingDailyLimit() {
                return 1;
        }

        @Override
        public Integer getMinUrlLength() {
                return 5;
        }

        @Override
        public Integer getMinParameterLength() {
                return 5;
        }

        @Override
        public Integer getMinVariableLength() {
                return 15;
        }
    
}


//@SpringBootTest(classes = IntegrationTestConfig.class)
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
    
    @Autowired
    protected UrlProcessor urlProcessor;
    
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @LocalServerPort
    private int port;

    protected String getUrlEncodePrefix() {
        return "localhost:" + port + "/";
    }



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


    protected Company setUpCompany(String subscriptionName) {

        Subscription sub = subscriptionName.equals("test") ? new subForTest() : SubscriptionManager.getSubscription(subscriptionName);

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

    protected Company setUpCompany() {
        return setUpCompany("TIER_1");
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
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
        
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

        Company company = setUpCompany();

        Role role = RoleManager.ROLES.get(new Random().nextInt(RoleManager.ROLES.size()));

        AppUser user = setUpUser(company, role, true);

    
        // Test with various invalid URLs
        String[] invalidUrls = {
            "not-a-valid-url",
            "http://",
            "https://",
            "ftp://example.com",  // unsupported protocol
            "www.example.com",    // missing protocol
            "http:example.com",   // missing slashes
            "http:/example.com",  // missing slash
            "http//example.com",  // missing colon
            ".com",
            "example",
            "http://.com",        // missing domain
            "http://example..com", // double dot
            "http://exam ple.com", // space in domain
            "http://exam\tple.com", // tab in domain
            "http://exam\nple.com", // newline in domain
            "http://example.com:abc", // invalid port
            "::::",
            "http://@example.com" // invalid chars
        };
        

        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
                
        for (String invalidUrl : invalidUrls) {
            // Count repositories before attempting operation
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();

            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());


            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/url/encode?url=" + invalidUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            // Verify bad request
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                
            assertTrue(Objects.requireNonNull(response.getBody()).contains("Invalid URL"),
                "Exception message should mention 'Invalid URL' for: " + invalidUrl);
            
            // Verify no change in repository state
            assertEquals(companyCount, companyRepo.count(), "Company count should not change");
            assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
            assertEquals(userCount, userRepo.count(), "User count should not change");
            
            // Verify user encoding count not incremented
            AppUser verifyUser = userRepo.findById(user.getEmail()).get();
            assertEquals(0, verifyUser.getUrlEncodingCount(), "User encoding count should not change for invalid URL");


			// 11. Verify company URL data was updated
			CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

			List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeEncoded .containsAll(keysAfterEncoded) &&  keysAfterEncoded .containsAll(keysBeforeEncoded),
					"data encoded keys should not change");

			List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
					"data decoded keys should not change");

		}
    }
    
    @Test
    void testSuccessfulEncode() throws Exception {
        for (int i = 0; i < 10; i++ ) {
			// Set up company and authorized user
			Company company = setUpCompany();
			AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			
			// Find active domain
			TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
					company, TopLevelDomain.DomainState.ACTIVE).getFirst();
			
			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			for (int j = 0; j <= 5; j++) {
				// 4. Create a URL with active domain
				String originalUrl = "https://" + activeDomain.getDomain() + "/" + this.gen.randomAlphaString(25) + "/" + this.gen.randomAlphaString(25);

				// 5. Capture state before encoding
				long companyCount = companyRepo.count();
				long urlEncodingCount = urlEncodingRepo.count();
				long userCount = userRepo.count();
                long userEncodingCount = userRepo.findById(user.getEmail()).get().getUrlEncodingCount();

                CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();

				List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
				List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

				ResponseEntity<String> response = restTemplate.exchange(
						"/api/url/encode?url=" + originalUrl,
						HttpMethod.GET,
						requestEntity,
						String.class
				);

                assertEquals(HttpStatus.OK,response.getStatusCode(), "The call to the api must successful");

				// 7. Extract encoded URL from response
				JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
				String encodedUrl = jsonNode.get("encoded_url").asText();
				
				// 8. Verify encoded URL is not empty
				assertNotNull(encodedUrl, "Encoded URL should not be null");
				assertFalse(encodedUrl.isEmpty(), "Encoded URL should not be empty");
				
				// 9. Verify repository updates
				assertEquals(companyCount, companyRepo.count(), "Company count should not change");
				assertEquals(userCount, userRepo.count(), "User count should not change");
				assertEquals(urlEncodingCount + 1, urlEncodingRepo.count(), "URL encoding count should increase by 1");

				// verify the UrlEncoding object was created correctly
				List<UrlEncoding> userUrlEncodings = urlEncodingRepo.findByUser(user);
				assertEquals(userUrlEncodings.size(), j + 1, "There should be exactly one UrlEncoding object for the user");
				UrlEncoding urlEncoding = userUrlEncodings.get(j);
				assertEquals(urlEncoding.getUrl(), originalUrl, "The UrlEncoding object should have the correct original URL");
				assertEquals(urlEncoding.getUrlEncoded(), encodedUrl, "The UrlEncoding object should have the correct encoded URL");
				assertEquals(urlEncoding.getUrlEncodingCount(), j + 1, "The UrlEncoding object should have the correct url encoding count");
				

				// 10. Verify user encoding count was incremented
				AppUser userAfter = userRepo.findById(user.getEmail()).get();
				assertEquals(userEncodingCount + 1, userAfter.getUrlEncodingCount(),
						"User encoding count should increase by 1");
				
				// 11. Verify company URL data was updated
				CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

				List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
		
				assertTrue (!keysBeforeEncoded .containsAll(keysAfterEncoded) &&  keysAfterEncoded .containsAll(keysBeforeEncoded),
						"data encoded keys should change");

				List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
		
				assertTrue (! keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
						"data encoded keys should change");
		
				// 12. Decode URL to verify it matches original
				String decodedUrl = this.urlProcessor.decode(encodedUrl,
						activeDomain.getDomain(),
						getUrlEncodePrefix(),
						dataAfter.getDataDecoded());
				
				// The decoded URL should match the original URL
				assertEquals(originalUrl, decodedUrl, "Decoded URL should match original URL");
			}
		}
	}

	@Test 
	void testVerifyDailyLimit() {
        
		String tier = "test";
        for (int i = 0; i < 10; i++) {
			// Set up company with specific tier
			Company company = setUpCompany(tier);
			
			// find the company's active domain
			TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
				company, TopLevelDomain.DomainState.ACTIVE).getFirst();
			
			// Set up use
			AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			String validUrl = "https://" + activeDomain.getDomain() + "/something";

			// send a successful encoding request
			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(
				"/api/url/encode?url=" + validUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

			assertEquals(HttpStatus.OK, response.getStatusCode(), "The call to the api must successful");

			// the next step is to send a request that exceeds the daily limit 
			// make sure to save the state of the database before sending the request to compare it with the state after the request 

			// 5. Capture state before encoding
			long companyCount = companyRepo.count();
			long urlEncodingCount = urlEncodingRepo.count();
			long userCount = userRepo.count();
			long userEncodingCount = userRepo.findById(user.getEmail()).get().getUrlEncodingCount();

			CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
			List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
			List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            
			String anotherValidUrl = "https://" + activeDomain.getDomain() + "/something/something";

			ResponseEntity<String> invalidReqRes = restTemplate.exchange(
				"/api/url/encode?url=" + anotherValidUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

            // Verify bad request
            assertEquals(HttpStatus.BAD_REQUEST, invalidReqRes.getStatusCode());

			assertTrue(Objects.requireNonNull(invalidReqRes.getBody()).contains("daily limit"),
			"Exception message should mention 'daily limit' for tier");
            
            // Verify no change in repository state
            assertEquals(companyCount, companyRepo.count(), "Company count should not change");
            assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
            assertEquals(userCount, userRepo.count(), "User count should not change");
            
            // Verify user encoding count not incremented
            AppUser verifyUser = userRepo.findById(user.getEmail()).get();
            assertEquals(userEncodingCount, verifyUser.getUrlEncodingCount(), "User encoding count should not change for invalid URL");

			// Verify no change in repository state
			assertEquals(companyCount, companyRepo.count(), "Company count should not change");
			assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
			assertEquals(userCount, userRepo.count(), "User count should not change");

			// 11. Verify company URL data was updated
			CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

			List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeEncoded .containsAll(keysAfterEncoded) &&  keysAfterEncoded .containsAll(keysBeforeEncoded),
					"data encoded keys should not change");

			List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
					"data decoded keys should not change");
			
		}		
	}

    @Test
    void testInvalidCompanyUrl() {
		String lastCompanyDomain = null;
		
		for (int i = 0; i < 10; i++) {
			// Set up company with multiple domains
			Company company = setUpCompany("test");
			String currentCompanyDomain = this.topLevelDomainRepo.findByCompanyAndDomainState(
				company, TopLevelDomain.DomainState.ACTIVE).getFirst().getDomain();

			// Set up user
			AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

			// Create a URL that doesn't match any of the company's domains
			String invalidDomainUrl;
			
			if (i == 0) {
				invalidDomainUrl = "https://www.invalid-domain" + i + ".com/product/123";
            }
			else {
				// use the domain of the last created company (make sure that the request is still flagged as invalid)
				invalidDomainUrl = "https://" + lastCompanyDomain + "/product/123"; 
            }

            lastCompanyDomain = currentCompanyDomain;

			// Capture repository state before call
			long companyCount = companyRepo.count();
			long urlEncodingCount = urlEncodingRepo.count();
			long userCount = userRepo.count();
			long userEncodingCount = user.getUrlEncodingCount();

			CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
			List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
			List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());


			// send a successful encoding request
			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(
				"/api/url/encode?url=" + invalidDomainUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

			
			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertTrue(Objects.requireNonNull(response.getBody()).contains("does not match any of the user's company top level domains"),
				"Exception message should mention 'Invalid URL' for: " + invalidDomainUrl);


			// Verify repository state is unchanged
			assertEquals(companyCount, companyRepo.count(), "Company count should not change");
			assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
			assertEquals(userCount, userRepo.count(), "User count should not change");

			// Verify user encoding count not incremented
			AppUser verifyUser = userRepo.findById(user.getEmail()).get();
			assertEquals(userEncodingCount, verifyUser.getUrlEncodingCount(),
					"User encoding count should not change for invalid domain URL");

			// 11. Verify company URL data was updated
			CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

			List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeEncoded .containsAll(keysAfterEncoded) &&  keysAfterEncoded .containsAll(keysBeforeEncoded),
					"data encoded keys should not change");

			List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
					"data decoded keys should not change");

		}
    }

    @Test
    void testDeprecatedUrl() {
		// Test with URLs that use a deprecated company domain
		for (int i = 0; i < 50; i++) {
			// Set up company with multiple domains
			Company company = setUpCompany();
			
			// Find a deprecated domain for this company
			List<TopLevelDomain> deprecatedDomains = topLevelDomainRepo.findByCompanyAndDomainState(
				company, TopLevelDomain.DomainState.DEPRECATED);
			
			// Verify we have a deprecated domain to test with
			assertFalse(deprecatedDomains.isEmpty(), "Setup should create a deprecated domain");
			
			// Get the first deprecated domain
			String deprecatedDomain = deprecatedDomains.getFirst().getDomain();
			
			// Set up user
			AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
			
			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			// Create a URL using the deprecated domain
			// use long path segments (longer than minimum lengths) so they are actually encoded
			String deprecatedDomainUrl = "https://" + deprecatedDomain + "/" + this.gen.randomAlphaString(25) + "/" + this.gen.randomAlphaString(25);
			
			// Capture repository state before call
			long companyCount = companyRepo.count();
			long urlEncodingCount = urlEncodingRepo.count();
			long userCount = userRepo.count();
			long userEncodingCount = user.getUrlEncodingCount();
			
			CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
			List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
			List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			// The request should fail with UrlCompanyDomainExpired
			ResponseEntity<String> response = restTemplate.exchange(
				"/api/url/encode?url=" + deprecatedDomainUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);	

			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertTrue(Objects.requireNonNull(response.getBody()).contains("The url top level domain is deprecated"),
				"Exception message should mention domain is deprecated");


			// Verify repository state is unchanged
			assertEquals(companyCount, companyRepo.count(), "Company count should not change");
			assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
			assertEquals(userCount, userRepo.count(), "User count should not change");
			
			// Verify user encoding count not incremented
			AppUser verifyUser = userRepo.findById(user.getEmail()).get();
			assertEquals(userEncodingCount, verifyUser.getUrlEncodingCount(), 
				"User encoding count should not change for deprecated domain URL");
			
			// 11. Verify company URL data was updated
			CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

			List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeEncoded .containsAll(keysAfterEncoded) &&  keysAfterEncoded .containsAll(keysBeforeEncoded),
					"data encoded keys should not change");

			List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			assertTrue (keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
					"data decoded keys should not change");
		
		}
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
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
                
        // Get company URL data
        CompanyUrlData urlData = companyUrlDataRepo.findFirstByCompany(company).get();
        String domainHash = urlData.getCompanyDomainHashed();
        
        // Sample encoded URL
        String encodedUrl = "https://localhost:8080/" + domainHash + "/abc123";
        
        // Request to decode URL
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/decode?encodedUrl=" + encodedUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        // Verify unauthorized
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    void testInvalidDomainHash() {
        for (int i = 0; i < 100; i++) {
            // Set up company and authorized user
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            // Capture initial state
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();
            
            // Create an encoded URL with an invalid domain
            String invalidEncodedUrl = "https://localhost:8018/" + this.gen.randomAlphaString(20) + "/some_encoding";
            
            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(
				"/api/url/decode?encodedUrl=" + invalidEncodedUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

			assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
			assertTrue(Objects.requireNonNull(response.getBody()).contains("The encoded url does not match the user's company top level domain"),
				"Exception message should mention 'invalid domain hash' for: " + invalidEncodedUrl);

			// Verify database state is unchanged
            assertEquals(companyCount, companyRepo.count(), "The company should not change");
            assertEquals(urlEncodingCount, urlEncodingRepo.count(), "The url encoding count should not change");
            assertEquals(userCount, userRepo.count(), "The user count should not change");


            // Verify user encoding count not incremented
            AppUser verifyUser = userRepo.findById(user.getEmail()).get();
            assertEquals(verifyUser.getUrlEncodingCount(), user.getUrlEncodingCount(), "User encoding count should not change when daily limit exceeded");

            // Verify company URL data hasn't changed
            CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

            List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

            assertEquals(keysBeforeEncoded, keysAfterEncoded, "The data should not change");

            List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

            assertEquals(keysBeforeDecoded, keysAfterDecoded, "The data should not change");
        }
    }
    
    @Test
    void testSuccessfulEncodeDecode() throws Exception {
        for (int i = 0; i < 10; i++) {

            // Set up company and authorized user
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            
            // Find active domain
            TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                    company, TopLevelDomain.DomainState.ACTIVE).getFirst();
            
            // Create original URL with active domain
            String originalUrl = "https://" + activeDomain.getDomain() + "/product/123?param=value";
            
			HttpHeaders headers = createAuthHeaders(user);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

			ResponseEntity<String> encodeResponse = restTemplate.exchange(
				"/api/url/encode?url=" + originalUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);

            // Parse response to get encoded URL
            com.fasterxml.jackson.databind.JsonNode jsonNode = 
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(encodeResponse.getBody());
            String encodedUrl = jsonNode.get("encoded_url").asText();
            
            // Capture state after encoding
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();


            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            
			ResponseEntity<String> decodeResponse = restTemplate.exchange(
				"/api/url/decode?encodedUrl=" + encodedUrl,
				HttpMethod.GET,
				requestEntity,
				String.class
			);
			
			assertEquals(HttpStatus.OK, decodeResponse.getStatusCode());

            // Parse response to get decoded URL
            com.fasterxml.jackson.databind.JsonNode decodeJson = 
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(decodeResponse.getBody());
            String decodedUrl = decodeJson.get("decoded_url").asText();
            
            // Verify database state is unchanged after decoding
            assertEquals(companyCount, companyRepo.count(), "Company count should not change");
            assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change after decode");
            assertEquals(userCount, userRepo.count(), "User count should not change");
            
            // Verify company URL data hasn't changed
            CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

            List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

            assertEquals(keysBeforeEncoded, keysAfterEncoded, "The data should not change");

            List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

            assertEquals(keysBeforeDecoded, keysAfterDecoded, "The data should not change");

            // Verify decoded URL matches original
            assertEquals(originalUrl, decodedUrl, "Decoded URL should match the original URL");

        }

    }
}


@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationUrlHistoryTest extends IntegrationBaseTest {
    
    @Test
    void testUserWithoutToken() {
        // Set up company
        Company company = setUpCompany();
        
        // Create user WITHOUT token link (authorized=false)
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
        
        // Request to get history
        HttpHeaders headers = createAuthHeaders(user);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/url/history?page=0&size=10",
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
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        
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
                    "/api/url/encode?url=" + originalUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Test history size at each step
            ResponseEntity<String> historyResponse = restTemplate.exchange(
                    "/api/url/history?page=0&size=" + totalUrls,
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
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        
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
                    "/api/url/encode?url=" + currentUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            
            // Get history
            ResponseEntity<String> historyResponse = restTemplate.exchange(
                    "/api/url/history?page=0&size=" + (i + 1),
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