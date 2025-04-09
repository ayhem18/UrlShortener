package org.companyManagerApi.tests;

import java.text.SimpleDateFormat;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.access.SubscriptionManager;
import org.apiUtils.commonClasses.CommonExceptions;
import org.apiUtils.commonClasses.CommonExceptions.UserNotFoundException;
import org.apiUtils.commonClasses.TokenAuthController;
import org.apiUtils.commonClasses.UserDetailsImp;
import org.company.entities.Company;
import org.company.entities.CompanyUrlData;
import org.company.entities.TopLevelDomain;
import org.companyManagerApi.controllers.CompanyController;
import org.companyManagerApi.exceptions.CompanyMngExceptions;
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

    @AfterEach
    @BeforeEach
    protected void clear() {
        // Reset the repositories before each test
        companyRepo.deleteAll();
        companyUrlDataRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll();
        urlEncodingRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
    }



    // Helper method to set up a test company with domains
    protected Company setUpCompany(String subName) throws JsonProcessingException {
        Subscription sub = SubscriptionManager.getSubscription(subName);
        
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

    protected Company setUpCompany() throws JsonProcessingException {
        return setUpCompany("TIER_1");
    }


    // Helper method to set up a test user
    protected AppUser setUpUser(Company company, Role role, boolean authorized) throws JsonProcessingException {
        String username = "test_user_" + gen.randomAlphaString(10);
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
            TokenUserLink tokenUserLink = new TokenUserLink("token_link_" + username , token, user);
            tokenUserLinkRepo.save(tokenUserLink);
        }

        AppUser u = userRepo.save(user);

        // serialize it to simulate the effect of the registerUser endpoint
        objectMapper.writeValueAsString(u);

        return u;
    }

}


class CompanyCtrViewEndpointTests extends BaseTest {

    @Test
    void testUserWithNoToken() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
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


class SubscriptionEndpointsTests extends BaseTest {
    @Test
    void testUserNoToken() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // create a company
            Company company = setUpCompany();

            // create a user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
            UserDetails userDetails = new UserDetailsImp(user);

            // check the view subscription endpoint
            
            Exception exception = assertThrows(
                    TokenAuthController.TokenNotFoundException.class,
                    () -> companyController.viewSubscription(userDetails),
                    "Should throw TokenNotFoundException for unauthorized user"
                );

            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                    "His access might have been revoked");
        
            exception = assertThrows(
                TokenAuthController.TokenNotFoundException.class,
                () -> companyController.updateSubscription(SubscriptionManager.getSubscription("TIER_1").toString(), userDetails),
                "Should throw TokenNotFoundException for unauthorized user"
            );

            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                "His access might have been revoked");
        
        }

    }
    

    @Test
    void testSameSubscriptionInUpdate() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            for (String subName : SubscriptionManager.SUB_NAMES) {
                // create a company with a certain subscription
                Company company = setUpCompany(subName);

                AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

                // update the subscription to the same subscription
                
                Exception exception = assertThrows(
                    CompanyMngExceptions.SameSubscriptionInUpdateException.class,
                    () -> companyController.updateSubscription(subName, new UserDetailsImp(user)),
                    "Should throw SameSubscriptionInUpdateException for same subscription"
                );

                assertTrue(exception.getMessage().contains("The subscription is the same as the current one"),
                    "Same subscription in update.");
            }
        }
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void testSuccessfulUpdateSubscription() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            
            for (String subName : SubscriptionManager.SUB_NAMES) {

                for (String newSubName : SubscriptionManager.SUB_NAMES) {
                    if (subName.equals(newSubName)) {
                        continue;
                    }

                    Company company = setUpCompany(subName);

                    AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
    
                    // make sure the call does not throw an exception   
                    ResponseEntity<String> res = assertDoesNotThrow(
                        () -> companyController.updateSubscription(newSubName, new UserDetailsImp(user)),
                        "Should not throw an exception for successful update"
                    );

                    Company newComp = this.companyRepo.findById(company.getId()).get();
                    
                    // make sure the response matches the serialization of the new company
                    assertEquals(res.getBody(),objectMapper.writeValueAsString(newComp));
                    
                    assertEquals(newComp.getSubscription(), SubscriptionManager.getSubscription(newSubName));

                    // make the sure the other data was not changed
                    assertEquals(newComp.getId(), company.getId());
                    assertEquals(newComp.getCompanyName(), company.getCompanyName());
                    assertEquals(newComp.getCompanyAddress(), company.getCompanyAddress());
                    assertEquals(newComp.getEmailDomain(), company.getEmailDomain());
                    assertEquals(newComp.getOwnerEmail(), company.getOwnerEmail());
                    assertEquals(newComp.getVerified(), company.getVerified()); 
                }
            }

        }
    }   

}


@SuppressWarnings("OptionalGetWithoutIsPresent")
class DomainEndpointsTests extends BaseTest {

    @Test
    void testUserNoToken() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // create a company
            Company company = setUpCompany();

            // create a user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
            UserDetails userDetails = new UserDetailsImp(user);

            // check the view subscription endpoint
            
            Exception exception = assertThrows(
                    TokenAuthController.TokenNotFoundException.class,
                    () -> companyController.updateDomain(null, false, userDetails),
                    "Should throw TokenNotFoundException for unauthorized user"
                );

            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                    "His access might have been revoked");   
        }
    }

    void testUpdateDomain(boolean deprecated) throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // create a company
            Company company = setUpCompany();

            // create a user
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);

            // extract the current domain
            TopLevelDomain currentDomain = this.topLevelDomainRepo.findFirstByCompanyAndDomainState(company, TopLevelDomain.DomainState.ACTIVE).get();

            String newDomain = ("www." + this.gen.randomAlphaString(10) + ".com").toLowerCase();

            // update the domain
            ResponseEntity<String> res = companyController.updateDomain(newDomain, deprecated, new UserDetailsImp(user));

            assertEquals(res.getBody(),objectMapper.writeValueAsString(company));

            // the next step is to check that the domain was updated
            Company fetchedCompany  = this.companyRepo.findById(company.getId()).get();
            // verify the fields are the same
            assertEquals(fetchedCompany.getId(), company.getId());
            assertEquals(fetchedCompany.getCompanyName(), company.getCompanyName());
            assertEquals(fetchedCompany.getCompanyAddress(), company.getCompanyAddress());
            assertEquals(fetchedCompany.getEmailDomain(), company.getEmailDomain());
            assertEquals(fetchedCompany.getOwnerEmail(), company.getOwnerEmail());
            assertEquals(fetchedCompany.getVerified(), company.getVerified());
            assertEquals(fetchedCompany.getSubscription(), company.getSubscription());

            // time to check the top level domain table
            TopLevelDomain fetchedDomain = this.topLevelDomainRepo.findFirstByCompanyAndDomainState(fetchedCompany, TopLevelDomain.DomainState.ACTIVE).get();
            assertEquals(fetchedDomain.getDomain(), newDomain);
            assertEquals(fetchedDomain.getDomainState(), TopLevelDomain.DomainState.ACTIVE);

            TopLevelDomain.DomainState oldDomainState = this.topLevelDomainRepo.findByDomain(currentDomain.getDomain()).get().getDomainState();
            if (deprecated) {
                assertEquals(oldDomainState, TopLevelDomain.DomainState.DEPRECATED);
            }
            else {
                assertEquals(oldDomainState, TopLevelDomain.DomainState.INACTIVE);
            }
        }
    }


    @Test
    void testSuccessfulUpdateDomain() throws JsonProcessingException {
        testUpdateDomain(true);
        testUpdateDomain(false);
    }
}


class TestUserView extends BaseTest {

    @Test
    void testNoToken() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
            Exception exception = assertThrows(
                TokenAuthController.TokenNotFoundException.class,
                () -> companyController.viewUser(null, new UserDetailsImp(user)),
                "Should throw TokenNotFoundException for unauthorized user"
            );
    
            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                "His access might have been revoked");
        }
    }

    
    @Test
    void testUserNotFound() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            
            Exception exception = assertThrows(
                UserNotFoundException.class,
                () -> companyController.viewUser(gen.randomAlphaString(100) + "@gmail.com", new UserDetailsImp(user)),
                "Should throw UserNotFoundException for non-existent user"
            );

            assertTrue(exception.getMessage().contains("There is no user with the email:"), "The error message is not correct");
        }
    }

    @Test
    void testUserInDifferentCompany() throws JsonProcessingException {

        Company diffCompany = setUpCompany();

        AppUser diffUser = setUpUser(diffCompany, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
        
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            Exception exception = assertThrows(
                UserNotFoundException.class,
                () -> companyController.viewUser(diffUser.getEmail(), new UserDetailsImp(user)),
                "Should throw UserNotFoundException for user in different company"
            );

            assertTrue(exception.getMessage().contains("There is no user with the email:"), "The error message is not correct");
            assertTrue(exception.getMessage().contains(company.getCompanyName()), "The error message is not correct");
        }
    }

    @Test
    void testUserWithNoAuthority() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();

            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);

            AppUser admin1 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser admin2 = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);

            AppUser employee1 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            AppUser employee2 = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);


            // admin trying to view the owner
            
            Exception exception = assertThrows(
                CommonExceptions.InsufficientRoleAuthority.class,
                () -> companyController.viewUser(owner.getEmail(), new UserDetailsImp(admin1)),
                "Should throw InsufficientRoleAuthority for user with no authority"
            );
            
            assertTrue(exception.getMessage().contains("Cannot view user with equal or higher priority"),
                "The error message is not correct");

            // admin trying to view another admin
            exception = assertThrows(
                CommonExceptions.InsufficientRoleAuthority.class,
                () -> companyController.viewUser(admin1.getEmail(), new UserDetailsImp(admin2)),
                "Should throw InsufficientRoleAuthority for user with no authority"
            );

            assertTrue(exception.getMessage().contains("Cannot view user with equal or higher priority"),
                "The error message is not correct");

            // employee trying to view any other user

            exception = assertThrows(
                CommonExceptions.InsufficientRoleAuthority.class,
                () -> companyController.viewUser(owner.getEmail(), new UserDetailsImp(employee1)),
                "Should throw InsufficientRoleAuthority for user with no authority"
            );

            assertTrue(exception.getMessage().contains("Cannot view user with equal or higher priority"),
                "The error message is not correct");


            exception = assertThrows(
                CommonExceptions.InsufficientRoleAuthority.class,
                () -> companyController.viewUser(admin1.getEmail(), new UserDetailsImp(employee2)),
                "Should throw InsufficientRoleAuthority for user with no authority"
            );

            assertTrue(exception.getMessage().contains("Cannot view user with equal or higher priority"),
                "The error message is not correct");

            exception = assertThrows(
                CommonExceptions.InsufficientRoleAuthority.class,
                () -> companyController.viewUser(employee1.getEmail(), new UserDetailsImp(employee2)),
                "Should throw InsufficientRoleAuthority for user with no authority"
            );

            assertTrue(exception.getMessage().contains("Cannot view user with equal or higher priority"),
                "The error message is not correct");
        }
    }

    @Test
    void testSuccessfulViewUser() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            AppUser admin = setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            AppUser employee = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            
            String ownerJson = objectMapper.writeValueAsString(owner);
            String adminJson = objectMapper.writeValueAsString(admin);
            String employeeJson = objectMapper.writeValueAsString(employee);
            
            // test the viewUser endpoint with the owner
            ResponseEntity<String> res1 = companyController.viewUser(null, new UserDetailsImp(owner));
            assertEquals(res1.getBody(), ownerJson);

            ResponseEntity<String> res2 = companyController.viewUser(admin.getEmail(), new UserDetailsImp(owner));
            assertEquals(res2.getBody(), adminJson);

            ResponseEntity<String> res3 = companyController.viewUser(employee.getEmail(), new UserDetailsImp(owner));
            assertEquals(res3.getBody(), employeeJson);            
        }
    }
}


class TestCompanyDeleteEndpoint extends BaseTest {

    @Test
    void testUserNoToken() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            Company company = setUpCompany();
            AppUser user = setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), false);
            
        Exception exception = assertThrows(
            TokenAuthController.TokenNotFoundException.class,
            () -> companyController.deleteCompany(new UserDetailsImp(user)),
            "Should throw TokenNotFoundException for unauthorized user"
            );

            assertTrue(exception.getMessage().contains("Their access might have been revoked."),
                "His access might have been revoked");
        }
    }   

    @Test
    void testDeleteCompany() throws JsonProcessingException {
        for (int i = 0; i < 10; i++) {
            // Create a company
            Company company = setUpCompany();

            // Create an owner and some other users
            AppUser owner = setUpUser(company, RoleManager.getRole(RoleManager.OWNER_ROLE), true);
            setUpUser(company, RoleManager.getRole(RoleManager.ADMIN_ROLE), true);
            setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);
            setUpUser(company, RoleManager.getRole(RoleManager.EMPLOYEE_ROLE), true);

            // Get counts before deletion
            long userCountBefore = userRepo.count();
            long tokenCountBefore = tokenRepo.count();
            long companyCountBefore = companyRepo.count();
            long topLevelDomainCountBefore = topLevelDomainRepo.count();

            // Get company users count
            List<AppUser> companyUsers = userRepo.findByCompany(company);
            int companyUserCount = companyUsers.size();

            // Get token count for company
            List<AppToken> companyTokens = tokenRepo.findByCompany(company);
            int companyTokenCount = companyTokens.size();
            
            // Get top level domain count for company
            List<TopLevelDomain> companyDomains = topLevelDomainRepo.findByCompany(company);
            int companyDomainCount = companyDomains.size();

            // Delete the company
            ResponseEntity<String> res = companyController.deleteCompany(new UserDetailsImp(owner));
            
            // Verify response contains the company JSON
            assertEquals(res.getBody(), objectMapper.writeValueAsString(company));

            // Verify all company-related entities are deleted
            assertEquals(userCountBefore - companyUserCount, userRepo.count(), 
                    "All users for the company should be deleted");
            
            assertEquals(tokenCountBefore - companyTokenCount, tokenRepo.count(), 
                    "All tokens for the company should be deleted");
            
            assertEquals(topLevelDomainCountBefore - companyDomainCount, topLevelDomainRepo.count(), 
                    "All top level domains for the company should be deleted");
            
            // Verify token user links are deleted
            assertEquals(0, tokenUserLinkRepo.findByUsersIn(companyUsers).size(),
                    "All token-user links for the company's users should be deleted");
            
            // Verify company count is reduced
            assertEquals(companyCountBefore - 1, companyRepo.count(), 
                    "The company itself should be deleted");
            
            // Verify company URL data is deleted
            List<CompanyUrlData> remainingUrlData = companyUrlDataRepo.findByCompany(company);
            assertTrue(remainingUrlData.isEmpty(), 
                    "All CompanyUrlData for the company should be deleted");
        }
    }
}
