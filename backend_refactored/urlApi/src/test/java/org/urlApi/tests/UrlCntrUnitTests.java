package org.urlApi.tests;

import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.TokenAuthController;
import org.apiUtils.commonClasses.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.stubs.repositories.*;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.url.UrlProcessor;
import org.urlApi.controllers.UrlController;
import org.urlApi.exceptions.UrlExceptions;
import org.user.entities.AppUser;
import org.user.entities.UrlEncoding;
import org.utils.CustomGenerator;

import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class BaseTest {

    protected final StubCompanyRepo companyRepo;
    protected final StubCompanyUrlDataRepo companyUrlDataRepo;
    protected final StubTopLevelDomainRepo topLevelDomainRepo;
    protected final StubUserRepo userRepo;
    protected final StubUrlEncodingRepo urlEncodingRepo;
    protected final StubTokenRepo tokenRepo;  
    protected final StubTokenUserLinkRepo tokenUserLinkRepo;
    protected final CustomGenerator gen;
    protected final UrlProcessor urlProcessor;
    protected final PasswordEncoder encoder;

    public BaseTest() {
        companyRepo = new StubCompanyRepo();
        companyUrlDataRepo = new StubCompanyUrlDataRepo();
        topLevelDomainRepo = new StubTopLevelDomainRepo(companyRepo);
        userRepo = new StubUserRepo(companyRepo);
        urlEncodingRepo = new StubUrlEncodingRepo();
        tokenRepo = new StubTokenRepo(companyRepo);
        tokenUserLinkRepo = new StubTokenUserLinkRepo(this.tokenRepo, this.userRepo);
        gen = new CustomGenerator();
        urlProcessor = new UrlProcessor(gen);
        encoder = new BCryptPasswordEncoder();
    }

    protected void clear() {
        // Reset the repositories before each test
        companyRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll();
        urlEncodingRepo.deleteAll();        
    }


    // Helper method to set up a test company with domains
    protected Company setUpCompany() {
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
            SubscriptionManager.getSubscription("TIER_1")
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
        String username = "test_user_" + gen.randomAlphaString(5);
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
        AppToken token = new AppToken(gen.randomString(12), encoder.encode("tokenId"), company, role );
        token.activate();
        tokenRepo.save(token);

        if (authorized) {
            // create a token user link for the user
            TokenUserLink tokenUserLink = new TokenUserLink("link_id", token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        return userRepo.save(user);

    }

}


class UrlEncodeTest extends BaseTest {

    private UrlController urlController;

    public UrlEncodeTest() {

        urlController = new UrlController(
            companyUrlDataRepo,
            urlEncodingRepo,
            topLevelDomainRepo,
            userRepo,
            tokenUserLinkRepo,
            urlProcessor,
            10
        );
    }

    @BeforeEach
    void setUp() {
        clear();
    }   


    @Test
    void testUnauthorizedUser() {
        for (int i = 0; i < 100; i++) {
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            UserDetails userDetails = new UserDetailsImp(user);

            String randomUrl = "www." + gen.randomAlphaString(10) + ".com";

            Exception exception = assertThrows(
                    TokenAuthController.TokenNotFoundException.class,
                    () -> urlController.encodeUrl(randomUrl, userDetails),
                    "Should throw TokenNotFoundException for unauthorized user"
            );

            assertTrue(exception.getMessage().contains("His access might have been revoked"),
                    "His access might have been revoked");
        }
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testInvalidUrl() {
        // Setup company and user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        UserDetails userDetails = new UserDetailsImp(user);
        
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
//            "http://example",     // missing TLD (it seems that the UrlValidator implementation does not flag this url as invalid)
            "http://.com",        // missing domain
            "http://example..com", // double dot
            "http://exam ple.com", // space in domain
            "http://exam\tple.com", // tab in domain
            "http://exam\nple.com", // newline in domain
            "http://example.com:abc", // invalid port
            "::::",
            "http://@example.com" // invalid chars
        };
        
        for (String invalidUrl : invalidUrls) {
            // Count repositories before attempting operation
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();
            

            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

            // Test the invalid URL
            Exception exception = assertThrows(
                UrlExceptions.InvalidUrlException.class,
                () -> urlController.encodeUrl(invalidUrl, userDetails),
                "Should throw InvalidUrlException for invalid URL: " + invalidUrl
            );
            
            assertTrue(exception.getMessage().contains("Invalid URL"),
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


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testVerifyDailyLimit() {
        // Create a spy of StubUrlEncodingRepo
        StubUrlEncodingRepo spyRepo = Mockito.spy(new StubUrlEncodingRepo());
        
        // Configure the spy to return a large list for findByUserAndUrlEncodingTimeAfter
        Mockito.doAnswer(invocation -> {
            // Get the user from the method arguments
            AppUser user = invocation.getArgument(0);
            // Return a list with 1000 dummy encodings (more than any tier's limit)
            List<UrlEncoding> result = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                result.add(new UrlEncoding(user, "dummy-original-" + i, "dummy-encoded-" + i, i + 1));
            }
            return result;
        }).when(spyRepo).findByUserAndUrlEncodingTimeAfter(Mockito.any(), Mockito.any());
        
        // Replace the standard stub with our spy
        
        urlController = new UrlController(
            companyUrlDataRepo, 
            spyRepo,
            topLevelDomainRepo,
            userRepo,
            tokenUserLinkRepo,
            urlProcessor,
            10
        );
        
        // Test with all subscription tiers that have daily limits
        String[] tiers = {
            "FREE",
            "TIER_1",
        };
        
        for (int i = 0; i < 10; i++) {
            for (String tier : tiers) {
                // Set up company with specific tier
                Company company = setUpCompany();
                company.setSubscription(SubscriptionManager.getSubscription(tier));
                companyRepo.save(company);

                // Set up user
                AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
                UserDetails userDetails = new UserDetailsImp(user);

                // Valid URL that should work if not for the limit
                String validUrl = "https://www.validUrl.com";

                CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
                List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
                List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

                // Count repositories before attempting operation
                long companyCount = companyRepo.count();
                long urlEncodingCount = urlEncodingRepo.count();
                long userCount = userRepo.count();

                // The request should fail with DailyLimitExceededException
                Exception exception = assertThrows(
                UrlExceptions.DailyLimitExceededException.class,
                () -> urlController.encodeUrl(validUrl, userDetails),
                "Should throw DailyLimitExceededException for tier: " + tier
                );

                assertTrue(exception.getMessage().contains("daily limit"),
                "Exception message should mention 'daily limit' for tier: " + tier);

                // Verify no change in repository state
                assertEquals(companyCount, companyRepo.count(), "Company count should not change");
                assertEquals(urlEncodingCount, urlEncodingRepo.count(), "URL encoding count should not change");
                assertEquals(userCount, userRepo.count(), "User count should not change");
                
                // Verify user encoding count not incremented
                AppUser verifyUser = userRepo.findById(user.getEmail()).get();
                assertEquals(verifyUser.getUrlEncodingCount(), user.getUrlEncodingCount(), "User encoding count should not change when daily limit exceeded");

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


    @Test
    void testVerifyDailyLimitNull() {

        // Create a spy of StubUrlEncodingRepo
        StubUrlEncodingRepo spyRepo = Mockito.spy(new StubUrlEncodingRepo());

        // Configure the spy to return a large list for findByUserAndUrlEncodingTimeAfter
        Mockito.doAnswer(invocation -> {
            // Get the user from the method arguments
            AppUser user = invocation.getArgument(0);
            // Return a list with 1000 dummy encodings (more than any tier's limit)
            List<UrlEncoding> result = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                result.add(new UrlEncoding(user, "dummy-original-" + i, "dummy-encoded-" + i, i + 1));
            }
            return result;
        }).when(spyRepo).findByUserAndUrlEncodingTimeAfter(Mockito.any(), Mockito.any());


        urlController = new UrlController(
            companyUrlDataRepo, 
            urlEncodingRepo,
            topLevelDomainRepo,
            userRepo,
            tokenUserLinkRepo,
            urlProcessor,
            10
        );
        
        // Create a custom subscription with null daily limit
        Subscription nullLimitSubscription = SubscriptionManager.getSubscription("TIER_INFINITY");

        for (int i = 0; i <= 20; i++) {
            // Set up company with null-limit subscription
            Company company = setUpCompany();
            company.setSubscription(nullLimitSubscription);
            companyRepo.save(company);

            // Set up user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            UserDetails userDetails = new UserDetailsImp(user);

            String validUrl = "https://www.testcompany.com/enterprise/data";

            // verify that calling the encodeUrl method does not throw a DailyLimitExceededException
            try {
                urlController.encodeUrl(validUrl, userDetails);
            } catch (Exception e) {
                assertFalse(e instanceof UrlExceptions.DailyLimitExceededException, "Should not throw DailyLimitExceededException for null-limit subscription");
            }
        }
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testInvalidCompanyDomain() {
        // Test with URLs that don't match any company top level domain
        for (int i = 0; i < 50; i++) {
            // Set up company with multiple domains
            Company company = setUpCompany();
            
            // Set up user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            UserDetails userDetails = new UserDetailsImp(user);
            
            // Create a URL that doesn't match any of the company's domains
            String invalidDomainUrl = "https://www.invalid-domain" + i + ".com/product/123";
            
            // Capture repository state before call
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();
            long userEncodingCount = user.getUrlEncodingCount();

            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());


            // The request should fail with InvalidTopLevelDomainException
            Exception exception = assertThrows(
                UrlExceptions.InvalidTopLevelDomainException.class,
                () -> urlController.encodeUrl(invalidDomainUrl, userDetails),
                "Should throw InvalidTopLevelDomainException for URL with non-matching domain"
            );
            
            assertTrue(exception.getMessage().contains("does not match any of the user's company top level domains"),
                "Exception message should mention domain mismatch");
            
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


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testDeprecatedCompanyDomain() {
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
            UserDetails userDetails = new UserDetailsImp(user);
            
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
            Exception exception = assertThrows(
                UrlExceptions.UrlCompanyDomainExpired.class,
                () -> urlController.encodeUrl(deprecatedDomainUrl, userDetails),
                "Should throw UrlCompanyDomainExpired for URL with deprecated domain"
            );
            
            assertTrue(exception.getMessage().contains("deprecated"),
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


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulActiveUrlEncoding() throws Exception {
        // Test successful encoding with active domain
        for (int i = 0; i < 50; i++) {
            // 1. Set up company with domains
            Company company = setUpCompany();

            // 2. Find the active domain
            List<TopLevelDomain> activeDomains = topLevelDomainRepo.findByCompanyAndDomainState(
                    company, TopLevelDomain.DomainState.ACTIVE);

            assertFalse(activeDomains.isEmpty(), "Setup should create an active domain");
            String activeDomain = activeDomains.getFirst().getDomain();

            // 3. Set up user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            UserDetails userDetails = new UserDetailsImp(user);

            for (int j = 0; j <= 5; j++) {
                // 4. Create a URL with active domain
                String originalUrl = "https://" + activeDomain + "/" + this.gen.randomAlphaString(25) + "/" + this.gen.randomAlphaString(25);

                // 5. Capture state before encoding
                long companyCount = companyRepo.count();
                long urlEncodingCount = urlEncodingRepo.count();
                long userCount = userRepo.count();
                long userEncodingCount = user.getUrlEncodingCount();

                CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
                List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
                List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

                // 6. Call the encoding endpoint
                String responseBody = null;
                try {
                    var response = urlController.encodeUrl(originalUrl, userDetails);
                    assertEquals(200, response.getStatusCode().value(), "Response status should be 200 OK");
                    responseBody = response.getBody();
                    assertNotNull(responseBody, "Response body should not be null");
                } catch (Exception e) {
                    fail("Encoding should succeed for active domain: " + e.getMessage());
                }

                // 7. Extract encoded URL from response
                com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
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
                AppUser verifyUser = userRepo.findById(user.getEmail()).get();
                assertEquals(userEncodingCount + 1, verifyUser.getUrlEncodingCount(),
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
                String decodedUrl = urlProcessor.decode(encodedUrl,
                        activeDomain,
                        urlController.getUrlEncodePrefix(),
                        dataAfter.getDataDecoded());

                // The decoded URL should match the original URL
                assertEquals(originalUrl, decodedUrl, "Decoded URL should match original URL");
            }
        }
    }

    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulInactiveUrlEncoding() throws Exception {
        // Test successful encoding with inactive domain (should result in transformation to active domain)
        for (int i = 0; i < 50; i++) {
            // 1. Set up company with domains
            Company company = setUpCompany();
            
            // 2. Find the inactive and active domains
            List<TopLevelDomain> inactiveDomains = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.INACTIVE);
            
            List<TopLevelDomain> activeDomains = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE);
            
            assertFalse(inactiveDomains.isEmpty(), "Setup should create an inactive domain");
            assertFalse(activeDomains.isEmpty(), "Setup should create an active domain");
            
            String inactiveDomain = inactiveDomains.getFirst().getDomain();
            String activeDomain = activeDomains.getFirst().getDomain();
            
            // 3. Set up user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            UserDetails userDetails = new UserDetailsImp(user);
            
            for (int j = 0; j <= 5; j++) {

                String sp1 = this.gen.randomAlphaString(25);
                String sp2 = this.gen.randomAlphaString(25);

                // 4. Create a URL with inactive domain
                String originalUrl = "https://" + inactiveDomain + "/" + sp1 + "/" + sp2;
                
                // Create the equivalent URL with active domain
                String activeEquivalentUrl = "https://" + activeDomain + "/" + sp1 + "/" + sp2;
                
                // 5. Capture state before encoding
                long companyCount = companyRepo.count();
                long urlEncodingCount = urlEncodingRepo.count();
                long userCount = userRepo.count();
                long userEncodingCount = user.getUrlEncodingCount();
                CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();

                List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
                List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());

                // 6. Call the encoding endpoint
                String responseBody = null;
                try {
                    var response = urlController.encodeUrl(originalUrl, userDetails);
                    assertEquals(200, response.getStatusCode().value(), "Response status should be 200 OK");
                    responseBody = response.getBody();
                    assertNotNull(responseBody, "Response body should not be null");
                } catch (Exception e) {
                    fail("Encoding should succeed for inactive domain: " + e.getMessage());
                }
                
                // 7. Extract encoded URL and warning from response
                com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
                String encodedUrl = jsonNode.get("encoded_url").asText();
                
                // 8. Verify encoded URL is not empty
                assertNotNull(encodedUrl, "Encoded URL should not be null");
                assertFalse(encodedUrl.isEmpty(), "Encoded URL should not be empty");
                
                // 9. Verify warning is present
                assertTrue(jsonNode.has("warning"), "Response should include a warning for inactive domain");
                String warning = jsonNode.get("warning").asText();
                assertTrue(warning.contains("inactive"), "Warning should mention domain is inactive");
                
                // 10. Verify repository updates
                assertEquals(companyCount, companyRepo.count(), "Company count should not change");
                assertEquals(userCount, userRepo.count(), "User count should not change");
                assertEquals(urlEncodingCount + 1, urlEncodingRepo.count(), "URL encoding count should increase by 1");
                
                
                // verify the UrlEncoding object was created correctly
                List<UrlEncoding> userUrlEncodings = urlEncodingRepo.findByUser(user);
                assertEquals(userUrlEncodings.size(), j + 1, "There should be exactly one UrlEncoding object for the user");
                UrlEncoding urlEncoding = userUrlEncodings.get(j);
                assertEquals(urlEncoding.getUrl(), activeEquivalentUrl, "The UrlEncoding object should have the correct original URL");
                assertEquals(urlEncoding.getUrlEncoded(), encodedUrl, "The UrlEncoding object should have the correct encoded URL");
                assertEquals(urlEncoding.getUrlEncodingCount(), j + 1, "The UrlEncoding object should have the correct url encoding count");

                
                // 11. Verify user encoding count was incremented
                AppUser verifyUser = userRepo.findById(user.getEmail()).get();
                assertEquals(userEncodingCount + 1, verifyUser.getUrlEncodingCount(), 
                    "User encoding count should increase by 1");
                
                // 12. Verify company URL data was updated
                CompanyUrlData dataAfter = companyUrlDataRepo.findFirstByCompany(company).get();

                // make sure the data encoded changes
                List<String> keysAfterEncoded = new ArrayList<>(dataAfter.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
                // make sure the data decoded changes
                List<String> keysAfterDecoded = new ArrayList<>(dataAfter.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());


                assertTrue (! keysBeforeEncoded.containsAll(keysAfterEncoded) && keysAfterEncoded.containsAll(keysBeforeEncoded),
                    "data encoded keys should change");

                assertTrue (! keysBeforeDecoded.containsAll(keysAfterDecoded) && keysAfterDecoded.containsAll(keysBeforeDecoded),
                    "data decoded keys should change");
            
                assertEquals(this.urlEncodingRepo.count(), i * 6 + j + 1, "Encoded URL should match the active domain version");

                // 13. Decode URL to verify it matches the active domain version, not the original
                String decodedUrl = urlProcessor.decode(encodedUrl, 
                    activeDomain,
                    urlController.getUrlEncodePrefix(),
                    dataAfter.getDataDecoded());

                    
                // The decoded URL should match the URL with active domain
                assertEquals(activeEquivalentUrl, decodedUrl, 
                "Decoded URL should match equivalent URL with active domain");                
            }
        }
    }
} 


class UrlDecodeTest extends BaseTest {

    private final UrlController urlController;

    public UrlDecodeTest() {
        super();
        urlController = new UrlController(companyUrlDataRepo, urlEncodingRepo, topLevelDomainRepo, userRepo, tokenUserLinkRepo, urlProcessor, 18);
    }

    @BeforeEach
    void setUp() {
        clear();
    }
    
    /**
     * Test 1: Verify that user without token cannot decode URLs
     */
    @Test
    void testUserWithoutToken() {
        
        for (int i = 0; i < 100; i++) {

            // Set up company
            Company company = setUpCompany();
                
            // Create user WITHOUT token link (authorized=false)
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
            // Create mock user details
            UserDetailsImp userDetails = new UserDetailsImp(user);
            
            // Find active domain
            TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                    company, TopLevelDomain.DomainState.ACTIVE).getFirst();
            
            // Sample encoded URL to decode
            String encodedUrl = "https://localhost:8018/" + activeDomain.getDomain() + "/some_encoding";
            
            // Attempt to decode the URL - should throw exception
            TokenAuthController.TokenNotFoundException exception =
                assertThrows(TokenAuthController.TokenNotFoundException.class,
                () -> urlController.decodeUrl(encodedUrl, userDetails),
                "User without token should not be authorized to decode URLs");
        
            assertTrue(exception.getMessage().contains("His access might have been revoked"),
                    "Exception message should indicate authorization failure");
        
        }
    }
    
    /**
     * Test 2: Verify that encoded URL with invalid domain hash raises exception
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testInvalidDomainHash() {
        for (int i = 0; i < 100; i++) {
            // Set up company and authorized user
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            // Create mock user details
            UserDetailsImp userDetails = new UserDetailsImp(user);

            // Capture initial state
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();
            
            // Create an encoded URL with an invalid domain
            String invalidEncodedUrl = "https://localhost:8018/" + this.gen.randomAlphaString(20) + "/some_encoding";
            
            CompanyUrlData dataBefore = companyUrlDataRepo.findFirstByCompany(company).get();
            List<String> keysBeforeEncoded = new ArrayList<>(dataBefore.getDataEncoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());
            List<String> keysBeforeDecoded = new ArrayList<>(dataBefore.getDataDecoded().stream().map(Map::keySet).flatMap(Collection::stream).toList());


            // Attempt to decode - should throw exception
            assertThrows(UrlExceptions.InvalidTopLevelDomainException.class, 
                () -> urlController.decodeUrl(invalidEncodedUrl, userDetails),
                "Should reject URL with invalid domain");

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
    
    /**
     * Test 3: Verify full encode-decode workflow functions correctly
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulEncodeDecode() throws Exception {
        for (int i = 0; i < 100; i++) {

            // Set up company and authorized user
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            // Create mock user details
            UserDetailsImp userDetails = new UserDetailsImp(user);
            
            // Find active domain
            TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                    company, TopLevelDomain.DomainState.ACTIVE).getFirst();
            
            // Create original URL with active domain
            String originalUrl = "https://" + activeDomain.getDomain() + "/product/123?param=value";
            
            // Step 1: Encode the URL
            var encodeResponse = urlController.encodeUrl(originalUrl, userDetails);
            assertEquals(200, encodeResponse.getStatusCode().value(), "Encode should succeed with status 200");
            
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
            
            
            // Step 2: Decode the URL
            var decodeResponse = urlController.decodeUrl(encodedUrl, userDetails);
            assertEquals(200, decodeResponse.getStatusCode().value(), "Decode should succeed with status 200");
            
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


class UrlHistoryTest extends BaseTest {

    private final UrlController urlController;

    public UrlHistoryTest() {
        super();
        urlController = new UrlController(companyUrlDataRepo, urlEncodingRepo, topLevelDomainRepo, userRepo, tokenUserLinkRepo, urlProcessor, 10);
    }

    @BeforeEach
    void setUp() {
        clear();
    }
    
    /**
     * Test 1: Verify that user without token cannot access URL history
     */
    @Test
    void testUserWithoutToken() {
        // Set up company
        Company company = setUpCompany();
        
        // Create user WITHOUT token link (authorized=false)
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
        
        // Create mock user details
        UserDetailsImp userDetails = new UserDetailsImp(user);
        
        // Attempt to access history - should throw exception
        TokenAuthController.TokenNotFoundException exception =
            assertThrows(TokenAuthController.TokenNotFoundException.class,
                () -> urlController.getHistory(0, 10, userDetails),
                "User without token should not be authorized to access history");
        
        assertTrue(exception.getMessage().contains("His access might have been revoked"), 
                "Exception message should indicate authorization failure");
                
        // Also test the parameterless overload
        assertThrows(TokenAuthController.TokenNotFoundException.class,
            () -> urlController.getHistory(null, null, userDetails),
            "User without token should not be authorized to access history (parameterless method)");
    }
    
    /**
     * Test 2: Verify history size respects company subscription limits
     */
    @Test
    void testHistorySizeLimit() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        
        // Get history size from subscription
        Integer historySize = company.getSubscription().getMaxHistorySize();
        assertNotNull(historySize, "History size should be defined in subscription");
        
        // Create mock user details
        UserDetailsImp userDetails = new UserDetailsImp(user);
        
        // Find active domain for URL creation
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();
        
        // Create and encode a series of URLs (more than the history size)
        int totalUrls = historySize + 15;
        for (int i = 0; i < totalUrls; i++) {
            String originalUrl = "https://" + activeDomain.getDomain() + "/product/" + i;
            urlController.encodeUrl(originalUrl, userDetails);
            
            // Test history size at each step
            var historyResponse = urlController.getHistory(0, totalUrls, userDetails);
            com.fasterxml.jackson.databind.JsonNode jsonNode = 
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(historyResponse.getBody());
            
            // For values less than history size, response size should match the loop index
            // For values beyond history size, response size should be capped at history size
            int expectedSize = Math.min(i + 1, historySize);
            assertEquals(expectedSize, jsonNode.size(), 
                    "History size should be " + expectedSize + " after " + (i + 1) + " URLs");
        }
        
        // Test with the parameterless method too
        var fullHistoryResponse = urlController.getHistory(null, null, userDetails);
        com.fasterxml.jackson.databind.JsonNode jsonNode = 
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(fullHistoryResponse.getBody());
        assertEquals(historySize, jsonNode.size(), 
                "Full history should respect subscription size limit");
    }
    
    /**
     * Test 3: Verify history is returned in correct order (newest first)
     */
    @Test
    void testHistoryOrder() throws Exception {
        // Set up company and authorized user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        
        // Create mock user details
        UserDetailsImp userDetails = new UserDetailsImp(user);
        
        // Find active domain for URL creation
        TopLevelDomain activeDomain = topLevelDomainRepo.findByCompanyAndDomainState(
                company, TopLevelDomain.DomainState.ACTIVE).getFirst();

        int hisSize = company.getSubscription().getMaxHistorySize();

        // Create a list of URLs to encode
        List<String> originalUrls = new ArrayList<>();
        for (int i = 0; i < hisSize + 10; i++) {
            originalUrls.add("https://" + activeDomain.getDomain() + "/page/" + i);
        }
        
        // Encode each URL in sequence and verify history order
        for (int i = 0; i < originalUrls.size(); i++) {
            String currentUrl = originalUrls.get(i);
            urlController.encodeUrl(currentUrl, userDetails);
            
            // Get history
            var historyResponse = urlController.getHistory(0, i + 1, userDetails);
            com.fasterxml.jackson.databind.JsonNode jsonNode = 
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(historyResponse.getBody());
            
            // Verify size matches expected
            int expectedSize = Math.min(i + 1, hisSize);
            assertEquals(expectedSize, jsonNode.size(), "History should have " + (i + 1) + " entries");
            
            // Verify order (newest first)

            for (int j = 0; j < expectedSize; j++) {
                String expectedUrl = originalUrls.get(i - j); // Reverse order
                String actualUrl = jsonNode.get(j).get("url").asText();
                assertEquals(expectedUrl, actualUrl, 
                        "History entry at position " + j + " should match URL at " + (i - j));
            }
        }
        
        // Test with parameterless method too
        var fullHistoryResponse = urlController.getHistory(null, null, userDetails);
        com.fasterxml.jackson.databind.JsonNode fullJsonNode = 
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(fullHistoryResponse.getBody());
        
        // Verify full history has all entries in reverse order
        assertEquals(hisSize, fullJsonNode.size(), "parameterless request should return as many items as the history size");
        for (int j = 0; j < hisSize; j++) {
            String expectedUrl = originalUrls.get(originalUrls.size() - 1 - j); // Reverse order
            String actualUrl = fullJsonNode.get(j).get("url").asText();
            assertEquals(expectedUrl, actualUrl, 
                    "Full history entry at position " + j + " should match URL in reverse order");
        }
    }
}