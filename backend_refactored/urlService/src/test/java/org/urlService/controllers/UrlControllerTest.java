package org.urlService.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.configurations.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.stubs.repositories.*;
import org.url.UrlProcessor;
import org.urlService.exceptions.UrlExceptions;
import org.user.entities.AppUser;
import org.user.entities.UrlEncoding;
import org.utils.CustomGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UrlControllerTest {

    private StubCompanyRepo companyRepo;
    private StubCompanyUrlDataRepo companyUrlDataRepo;
    private StubTopLevelDomainRepo topLevelDomainRepo;
    private StubUserRepo userRepo;
    private StubUrlEncodingRepo urlEncodingRepo;
    private UrlController urlController;
    private CustomGenerator gen;
    private UrlProcessor urlProcessor;
    private PasswordEncoder encoder;
    private static final int TEST_PORT = 8080;

    @BeforeEach
    void setUp() {
        companyRepo = new StubCompanyRepo();
        companyUrlDataRepo = new StubCompanyUrlDataRepo();
        topLevelDomainRepo = new StubTopLevelDomainRepo(companyRepo);
        userRepo = new StubUserRepo(companyRepo);
        urlEncodingRepo = new StubUrlEncodingRepo();
        gen = new CustomGenerator();
        urlProcessor = new UrlProcessor(gen);
        encoder = new BCryptPasswordEncoder();

        urlController = new UrlController(
            companyUrlDataRepo, 
            urlEncodingRepo,
            topLevelDomainRepo,
            userRepo,
            urlProcessor,
            TEST_PORT
        );
    }

    // Helper method to set up a test company with domains
    private Company setUpCompany() {
        String companyId = gen.randomAlphaString(12);
        String companyName = gen.randomAlphaString(10);
        String companyEmailDomain = gen.randomAlphaString(5);
        String companyEmail = "owner@" + companyEmailDomain + ".com";

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
        String activeDomainName = "www." + companyName + "_active.com";
        TopLevelDomain activeDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            activeDomainName,
            encoder.encode(activeDomainName),
            testCompany
        );
        topLevelDomainRepo.save(activeDomain);
        
        // Create inactive domain
        String inactiveDomainName = "www." + companyName + "_inactive.com";
        TopLevelDomain inactiveDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            inactiveDomainName,
            encoder.encode(inactiveDomainName),
            testCompany
        );
        inactiveDomain.deactivate();
        topLevelDomainRepo.save(inactiveDomain);

        // Create deprecated domain
        String deprecatedDomainName = "www." + companyName + "_deprecated.com";
        TopLevelDomain deprecatedDomain = new TopLevelDomain(
            gen.randomAlphaString(10),
            deprecatedDomainName,
            encoder.encode(deprecatedDomainName),
            testCompany
        );
        deprecatedDomain.deprecate();
        topLevelDomainRepo.save(deprecatedDomain);

        // Create company URL data
        CompanyUrlData companyUrlData = new CompanyUrlData(
            testCompany,
            this.encoder.encode(activeDomainName)
        );
        companyUrlDataRepo.save(companyUrlData);

        return testCompany;
    }

    // Helper method to set up a test user
    private AppUser setUpUser(Company company, Role role) {
        String username = "testuser_" + gen.randomAlphaString(5);
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
        userRepo.save(user);
        return user;
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testInvalidUrl() {
        // Setup company and user
        Company company = setUpCompany();
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));
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
            "http://example",     // missing TLD
            "http://.com",        // missing domain
            "http://example..com", // double dot
            "http://exam ple.com", // space in domain
            "http://exam\tple.com", // tab in domain
            "http://exam\nple.com", // newline in domain
            "http://exa#mple.com", // hash in domain
            "http://example.com:abc", // invalid port
            "::::",
            "http://@example.com" // invalid chars
        };
        
        for (String invalidUrl : invalidUrls) {
            // Count repositories before attempting operation
            long companyCount = companyRepo.count();
            long urlEncodingCount = urlEncodingRepo.count();
            long userCount = userRepo.count();
            
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
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testVerifyDailyLimit() {
        // Create a special stub repository that always returns list with entries matching the limit
        class DailyLimitStubUrlEncodingRepo extends StubUrlEncodingRepo {
            @Override
            public List<UrlEncoding> findByUserAndUrlEncodingTimeAfter(AppUser user, LocalDateTime time) {
                // Return a list with enough entries to exceed any limit
                List<UrlEncoding> result = new ArrayList<>();
                // Create 1000 dummy encodings (more than any tier's limit)
                for (int i = 0; i < 1000; i++) {
                    result.add(new UrlEncoding(user, "dummy-original-" + i, "dummy-encoded-" + i));
                }
                return result;
            }
        }
        
        // Replace the standard stub with our special stub
        urlEncodingRepo = new DailyLimitStubUrlEncodingRepo();
        urlController = new UrlController(
            companyUrlDataRepo, 
            urlEncodingRepo,
            topLevelDomainRepo,
            userRepo,
            urlProcessor,
            TEST_PORT
        );
        
        // Test with all subscription tiers that have daily limits
        String[] tiers = {
            "FREE",
            "TIER_1",
            "TIER_INFINITY"
        };
        
        for (String tier : tiers) {
            // Set up company with specific tier
            Company company = setUpCompany();
            company.setSubscription(SubscriptionManager.getSubscription(tier));
            companyRepo.save(company);
            
            // Set up user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));
            UserDetails userDetails = new UserDetailsImp(user);
            
            // Valid URL that should work if not for the limit
            String validUrl = "https://www.testcompany.com/products/123?ref=test";
            
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
            assertEquals(0, verifyUser.getUrlEncodingCount(), "User encoding count should not change when daily limit exceeded");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testVerifyDailyLimitNull() throws JsonProcessingException {
        // Create a special stub repository for subscription with null daily limit
        class NullLimitStubUrlEncodingRepo extends StubUrlEncodingRepo {
            @Override
            public List<UrlEncoding> findByUserAndUrlEncodingTimeAfter(AppUser user, LocalDateTime time) {
                // Return an empty list
                return new ArrayList<>();
            }
        }
        
        // Replace the standard stub with our special stub
        urlEncodingRepo = new NullLimitStubUrlEncodingRepo();
        urlController = new UrlController(
            companyUrlDataRepo, 
            urlEncodingRepo,
            topLevelDomainRepo,
            userRepo,
            urlProcessor,
            TEST_PORT
        );
        
        // Create a custom subscription with null daily limit
        Subscription nullLimitSubscription = SubscriptionManager.getSubscription("TIER_INFINITY");
        
        // Set up company with null-limit subscription
        Company company = setUpCompany();
        company.setSubscription(nullLimitSubscription);
        companyRepo.save(company);
        
        // Set up user
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE));
        UserDetails userDetails = new UserDetailsImp(user);
        
        // Valid URL that should work
        String validUrl = "https://www.testcompany.com/enterprise/data";
        
        // Count repositories before attempting operation
        long companyCount = companyRepo.count();
        long urlEncodingCount = urlEncodingRepo.count();
        long userCount = userRepo.count();
        long userEncodingCount = user.getUrlEncodingCount();
        
        // This should succeed because there's no daily limit
        var response = urlController.encodeUrl(validUrl, userDetails);
        
        // Verify successful response
        assertEquals(200, response.getStatusCode().value(), "Should return 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        // Verify a new URL encoding was added
        assertEquals(urlEncodingCount + 1, urlEncodingRepo.count(), "URL encoding count should increase by 1");
        
        // Verify user encoding count was incremented
        AppUser verifyUser = userRepo.findById(user.getEmail()).get();
        assertEquals(userEncodingCount + 1, verifyUser.getUrlEncodingCount(), "User encoding count should increase by 1");
        
        // Verify company count didn't change
        assertEquals(companyCount, companyRepo.count(), "Company count should not change");
        assertEquals(userCount, userRepo.count(), "User count should not change");
    }
} 