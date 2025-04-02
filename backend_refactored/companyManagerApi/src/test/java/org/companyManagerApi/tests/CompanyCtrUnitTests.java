package org.companyManagerApi.tests;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.access.Role;
import org.access.RoleManager;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.TokenAuthController;
import org.apiUtils.commonClasses.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.companyManagerApi.controllers.CompanyController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.stubs.repositories.StubCompanyRepo;
import org.stubs.repositories.StubCompanyUrlDataRepo;
import org.stubs.repositories.StubTokenRepo;
import org.stubs.repositories.StubTokenUserLinkRepo;
import org.stubs.repositories.StubTopLevelDomainRepo;
import org.stubs.repositories.StubUrlEncodingRepo;
import org.stubs.repositories.StubUserRepo;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.url.UrlProcessor;
import org.user.entities.AppUser;
import org.utils.CustomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class BaseTest {

    protected final StubCompanyRepo companyRepo;
    protected final StubCompanyUrlDataRepo companyUrlDataRepo;
    protected final StubTopLevelDomainRepo topLevelDomainRepo;
    protected final StubUserRepo userRepo;
    protected final StubUrlEncodingRepo urlEncodingRepo;
    protected final StubTokenRepo tokenRepo;  
    protected final StubTokenUserLinkRepo tokenUserLinkRepo;
    protected final CompanyController companyController;

    protected final CustomGenerator gen;
    protected final UrlProcessor urlProcessor;
    protected final PasswordEncoder encoder;

    protected final ObjectMapper objectMapper;

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

        companyController = new CompanyController(companyRepo,
                                topLevelDomainRepo,
                                userRepo,
                                tokenUserLinkRepo,
                                tokenRepo,
                                companyUrlDataRepo);

        // set the object mapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy hh:mm"));

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
    protected Company setUpCompany() throws JsonProcessingException {
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

        // before saving the company object, serialize it to simulate the effect of the endpoint
        objectMapper.writeValueAsString(testCompany);

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



class CompanyCtrViewEndpointTests extends BaseTest {

    @Test
    void testUserWithNoToken() throws JsonProcessingException {
        // create a company
        Company company = setUpCompany();

        // create a user
        AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
        
        UserDetails userDetails = new UserDetailsImp(user);

        Exception exception = assertThrows(
                TokenAuthController.TokenNotFoundException.class,
                () -> companyController.viewCompany(userDetails),
                "Should throw TokenNotFoundException for unauthorized user"
        );

        assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                "His access might have been revoked");
    }

    @Test
    void testSuccessfulViewCompany() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // create a company
            Company company = setUpCompany();

            // create a user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            ResponseEntity<String> res = companyController.viewCompany(new UserDetailsImp(user));

            assertEquals(res.getBody(),objectMapper.writeValueAsString(company));
        }
    }
}