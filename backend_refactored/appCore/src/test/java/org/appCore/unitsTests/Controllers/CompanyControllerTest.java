package org.appCore.unitsTests.Controllers;

import org.appCore.controllers.CompanyController;
import org.appCore.exceptions.CompanyAndUserExceptions;
import org.appCore.exceptions.CompanyExceptions;
import org.appCore.internal.StubCompanyRepo;
import org.appCore.internal.StubCounterRepo;
import org.appCore.internal.StubTokenRepo;
import org.appCore.internal.StubTokenUserLinkRepo;
import org.appCore.internal.StubTopLevelDomainRepo;
import org.appCore.internal.StubUserRepo;
import org.appCore.requests.CompanyRegisterRequest;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;
import org.utils.CustomGenerator;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.access.Role;
import org.access.RoleManager;
import org.access.SubscriptionManager;

import java.util.List;

public class CompanyControllerTest {

    private final StubCompanyRepo companyRepo;
    private final StubCounterRepo counterRepo;
    private final StubUserRepo userRepo;
    private final StubTopLevelDomainRepo topLevelDomainRepo;
    private final StubTokenRepo tokenRepo;
    private final StubTokenUserLinkRepo tokenUserLinkRepo;

    private final CustomGenerator gen = new CustomGenerator();
    private final CustomGenerator mockGen;
    private final CompanyController comCon;
    public static int COUNTER = 0;

    private CustomGenerator setMockCG() {
        CustomGenerator mockedCG = spy();
        when(mockedCG.randomString(anyInt())).then(
          invocation -> {CompanyControllerTest.COUNTER += 1; return "RANDOM_STRING_" + CompanyControllerTest.COUNTER;}
        );
        return mockedCG;
    }

    @BeforeEach
    public void setUp() {        
        // Clear repositories to ensure a clean state
        clearRepositories();
        
        // Initialize default test data for each repository
        
        // 1. Add default companies
        Company youtube = new Company("aaa", SubscriptionManager.getSubscription("TIER_1"), "owner@youtube.com", "youtube.com");
        Company github = new Company("bbb", SubscriptionManager.getSubscription("TIER_1"), "owner@github.com", "github.com");
        companyRepo.save(youtube);
        companyRepo.save(github);
        
        // 2. Add default domains for these companies
        TopLevelDomain youtubeDomain = new TopLevelDomain("domain1", "youtube.com", "hash_youtube", youtube);
        TopLevelDomain githubDomain = new TopLevelDomain("domain2", "github.com", "hash_github", github);
        topLevelDomainRepo.save(youtubeDomain);
        topLevelDomainRepo.save(githubDomain);
        
        // 3. Add default users for these companies
        AppUser youtubeOwner = new AppUser("owner@youtube.com", "ytowner", "password123", youtube, RoleManager.getRole(RoleManager.OWNER_ROLE));
        AppUser githubOwner = new AppUser("owner@github.com", "ghowner", "password123", github, RoleManager.getRole(RoleManager.OWNER_ROLE));
        userRepo.save(youtubeOwner);
        userRepo.save(githubOwner);
        
        // 4. Add default tokens for these companies
        AppToken youtubeToken = new AppToken("token_youtube", "hash_youtube", youtube, RoleManager.getRole(RoleManager.OWNER_ROLE));
        AppToken githubToken = new AppToken("token_github", "hash_github", github, RoleManager.getRole(RoleManager.OWNER_ROLE));

        youtubeToken.activate();
        githubToken.activate();

        AppToken adminYoutubeToken = new AppToken("token_youtube", "hash_youtube", youtube, RoleManager.getRole(RoleManager.ADMIN_ROLE));
        AppToken adminGithubToken = new AppToken("token_github", "hash_github", github, RoleManager.getRole(RoleManager.ADMIN_ROLE));

        tokenRepo.save(youtubeToken);
        tokenRepo.save(githubToken);

        tokenRepo.save(adminGithubToken);
        tokenRepo.save(adminYoutubeToken);


        // 5. Add default token-user links
        TokenUserLink youtubeLink = new TokenUserLink("youtubeLinked", youtubeToken, youtubeOwner);
        TokenUserLink githubLink = new TokenUserLink("githubLinked", githubToken, githubOwner);
        tokenUserLinkRepo.save(youtubeLink);
        tokenUserLinkRepo.save(githubLink);
        
        // 6. Ensure counter repository is initialized
        counterRepo.addCompanyCollection();
    }

    @AfterEach
    public void tearDown() {
        clearRepositories();
    }

    private void clearRepositories() {
        // Clear all repositories to ensure a clean state
        companyRepo.deleteAll();
        topLevelDomainRepo.deleteAll();
        userRepo.deleteAll(); 
        tokenRepo.deleteAll();
        tokenUserLinkRepo.deleteAll();
        // The counter repo might need special handling if it doesn't have a clear method
        // We might need to reset it to initial state instead
    }

    public CompanyControllerTest() throws NoSuchFieldException, IllegalAccessException {
        this.companyRepo = new StubCompanyRepo();
        this.topLevelDomainRepo = new StubTopLevelDomainRepo(this.companyRepo);
        this.counterRepo = new StubCounterRepo();
        this.userRepo = new StubUserRepo(this.companyRepo);
        this.tokenRepo = new StubTokenRepo(this.companyRepo);
        this.tokenUserLinkRepo = new StubTokenUserLinkRepo(this.tokenRepo, this.userRepo);
        
        
        this.mockGen = setMockCG();
        // set a stubCustomGenerator, so we can verify the registerCompany method properly
        
        this.comCon = new CompanyController(this.companyRepo,
                this.topLevelDomainRepo,
                this.userRepo,
                this.counterRepo,
                this.tokenRepo,
                this.tokenUserLinkRepo,
                this.gen,
                null);
    }

    @Test
    public void testMockCG() {
        // a small test to make sure the mock object works as expected
        for (int i = 0; i <= 100; i++) {
            Assertions.assertEquals(this.mockGen.randomString(10), "RANDOM_STRING_" + (i + 1));
        }
        for (int i = 0; i <= 100; i++) {
            assertNotNull(this.mockGen.generateId(i));
        }
    }

    //////////////////////// register a company ////////////////////////
    @Test
    void testUniquenessConstraints() {
        // 1. company ID uniqueness test 
        for (Company c : this.companyRepo.findAll()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(c.getId(),
                    "random_domain.com",
                    c.getSubscription().getTier(),
                    null,
                    null);

            Assertions.assertThrows(
                    CompanyExceptions.ExistingCompanyException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }

        // 2. top level domain uniqueness test
        for (TopLevelDomain domain : this.topLevelDomainRepo.findAll()) {
            // Generate a unique company ID for this test case
            String uniqueId = "unique_" + this.gen.randomString(8);

            // Create a request with unique ID but existing domain
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    domain.getDomain(),
                    "TIER_1",
                    "owner@" + domain.getDomain(),  // Use matching email for domain
                    domain.getDomain()
            );

            // Verify that an ExistingTopLevelDomainException is thrown
            Assertions.assertThrows(
                    CompanyExceptions.ExistingTopLevelDomainException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }

        // 3. owner email uniqueness test
        for (AppUser user : this.userRepo.findAll()) {
            // Get the user's email
            String userEmail = user.getEmail();

            // Extract domain from email for consistent request
            String emailDomain = userEmail.substring(userEmail.indexOf('@') + 1);

            // Generate unique domain and ID
            String uniqueId = "unique_" + this.gen.randomString(8);
            String uniqueDomain = "unique" + this.gen.randomString(5) + ".com";

            // Create request with unique ID, unique domain, but existing email
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                    uniqueId,
                    uniqueDomain,
                    "TIER_1",
                    userEmail,  // Existing user email
                    emailDomain
            );

            // Verify that a MultipleOwnersException is thrown
            Assertions.assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> this.comCon.registerCompany(req)
            );
        }

        // @Test
        // void testRegisterUniquenessConstraints() {
        //     for (CompanyWrapper w: this.companyRepo.getWrappers()) {
        //         // create a register request based on the given company
        //         CompanyRegisterRequest req = new CompanyRegisterRequest(this.gen.randomString(10),
        //                 w.getSite(),
        //                 w.getSubscription().getTier());

        //         Assertions.assertThrows(
        //                 CompanyExceptions.ExistingSiteException.class,
        //                 () -> this.comCon.registerCompany(req)
        //         );
        //     }
        // }

        // @Test
        // void testCompanyCount() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //     Method method = this.comCon.getClass().getDeclaredMethod("getCompanyCount");
        //     method.setAccessible(true);

        //     for (int i = 0; i < 100; i++) {
        //         long count = (long) method.invoke(this.comCon);
        //         assertEquals(count, i);
        //         assertEquals(i + 1, this.counterRepo.findById(Company.COMPANY_COLLECTION_NAME).get().getCount());
        //     }

        //     // make sure to delete all the
        //     this.counterRepo.deleteAll();
        // }

        // @Test
        // void testRegisterValidCompany() throws JsonProcessingException, IllegalAccessException {
        //     List<String> initialFieldsSerialization = List.of("id",
        //             "site",
        //             "siteHash",
        //             "roleTokens",
        //             "roleTokensHashed",
        //             "subscription");

        //     String id = "a_company_id";
        //     String site = "www.completelyNewSite.com";
        //     Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        //     CompanyRegisterRequest req = new CompanyRegisterRequest(id, site, sub.getTier());

        //     ResponseEntity<String> res = comCon.registerCompany(req);

        //     // the new company object must have been added to the database
        //     assertEquals(3, this.companyRepo.getDb().size());

        //     Company newC = this.companyRepo.getDb().get(2);

        //     // use reflection to make sure none of the fields are none
        //     List<Field> fields = List.of(Company.class.getDeclaredFields());
        //     for (Field f : fields) {
        //         f.setAccessible(true);
        //         assertNotNull(f.get(newC));
        //     }

        //     String body = res.getBody();

        //     // read the body to make sure the company record is saved correctly
        //     Object doc;
        //     doc = Configuration.defaultConfiguration().jsonProvider().parse(body);
        //     Set<String> keys = JsonPath.read(doc, "keys()");

        //     org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(initialFieldsSerialization);
        //     // the next step is to make sure everything is saved and serialized correctly

        //     // id
        //     String serializedId = JsonPath.read(doc, "$.id");
        //     assertEquals(id, serializedId);

        //     // the site
        //     String serializedSite = JsonPath.read(doc, "$.site");
        //     assertEquals(site, serializedSite);

        //     // site hash
        //     String siteHashSerialized = JsonPath.read(doc, "$.siteHash");
        //     assertEquals(this.gen.generateId(0), siteHashSerialized);

        //     // tokens
        //     Map<String, String> tokens = JsonPath.read(doc, "$.roleTokens");
        //     Map<String, String> expectedTokens = Map.of("owner", "RANDOM_STRING_1",
        //             "admin", "RANDOM_STRING_2",
        //             "registereduser", "RANDOM_STRING_3");
        //     assertEquals(expectedTokens, tokens);

        //     PasswordEncoder encoder = new BCryptPasswordEncoder();
        //     Map<String, String> hashedTokens = JsonPath.read(doc, "$.roleTokensHashed");

        //     assertTrue(encoder.matches("RANDOM_STRING_1", hashedTokens.get("owner")));
        //     assertTrue(encoder.matches("RANDOM_STRING_2", hashedTokens.get("admin")));
        //     assertTrue(encoder.matches("RANDOM_STRING_3", hashedTokens.get("registereduser")));

        //     // subscription
        //     String subSerializer = JsonPath.read(doc, "$.subscription");
        //     assertEquals(subSerializer, sub.getTier());
        // }


        // //////////////////////// view company details  ////////////////////////
        // @Test
        // void testViewNoExistingCompany() {
        //     for (int i = 0; i <= 100; i++) {
        //         String NoId = this.gen.randomString(100);
        //         assertThrows(CompanyExceptions.NoCompanyException.class, () -> this.comCon.viewCompanyDetails(NoId));
        //     }
        // }

        // @Test
        // void testViewCompanyDetails() throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {
        //     List<String> fields = List.of("site",
        //             "subscription");

        //     List<Company> db = this.companyRepo.getDb();

        //     Field fid = Company.class.getDeclaredField("id");
        //     fid.setAccessible(true);

        //     Field fSite = Company.class.getDeclaredField("site");
        //     fSite.setAccessible(true);

        //     Field fSub = Company.class.getDeclaredField("subscription");
        //     fSub.setAccessible(true);


        //     for (Company c : db) {
        //         // access the company id
        //         String id = (String) fid.get(c);
        //         assertDoesNotThrow(() -> comCon.viewCompanyDetails(id));

        //         ResponseEntity<String> re = comCon.viewCompanyDetails(id);
        //         // check the status code
        //         assertEquals(HttpStatus.OK, re.getStatusCode());
        //         String body = re.getBody();

        //         Object doc = Configuration.defaultConfiguration().jsonProvider().parse(body);
        //         Set<String> keys = JsonPath.read(doc, "keys()");

        //         // make sure the keys are correct
        //         org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(fields);

        //         String serializedSite = JsonPath.read(doc, "$.site");
        //         String serializedSub = JsonPath.read(doc, "$.subscription");

        //         // make sure the values are correct
        //         assertEquals(((Subscription) fSub.get(c)).getTier(), serializedSub);
        //         assertEquals(fSite.get(c), serializedSite);
        //     }
        // }

        // //////////////////////// delete a company  ////////////////////////

    }

    @Test 
    void testTokenConstraintsRegisterCompany() {
        
        for (String roleString : RoleManager.ROLES_STRING) {
            // 1. Generate a random company id that doesn't exist in the repo
            String randomCompanyId = "test_company_" + gen.randomAlphaString(8);
            while (companyRepo.existsById(randomCompanyId)) {
                randomCompanyId = "test_company_" + gen.randomAlphaString(8);
            }
            
            // Create a dummy company to associate with the token
            Company dummyCompany = new Company(
                randomCompanyId,
                SubscriptionManager.getSubscription("TIER_1"),
                "owner@example.com",
                "example.com"
            );
            
            // 2. Create and save a token for this company with the current role
            Role role = RoleManager.getRole(roleString);
            AppToken token = new AppToken(
                "token_" + randomCompanyId,
                "hash_" + randomCompanyId,
                dummyCompany,
                role
            );
            tokenRepo.save(token);
            
            // 3. Create a company register request with the same ID
            CompanyRegisterRequest req = new CompanyRegisterRequest(
                randomCompanyId,
                "unique" + gen.randomAlphaString(5) + ".com",
                "TIER_1",
                "newowner@example.com",
                "example.com"
            );
            
            // Final randomCompanyId for use in lambda
            String finalCompanyId = randomCompanyId;
            
            // Verify that registering a company with this ID throws an exception
            assertThrows(
                CompanyAndUserExceptions.MultipleOwnersException.class,
                () -> comCon.registerCompany(req),
                "Should fail when token already exists for company ID " + finalCompanyId
            );
        }
    }

    @Test
    void testRegisterCompany() {
        // 1. Generate a random company id that doesn't exist in the repo
        String randomCompanyId = "new_company_" + gen.randomAlphaString(8);
        while (companyRepo.existsById(randomCompanyId)) {
            randomCompanyId = "new_company_" + gen.randomAlphaString(8);
        }
        
        // Generate unique domain
        String domain = "unique" + gen.randomAlphaString(5) + ".com";
        String ownerEmail = "owner@" + domain;
        
        // 2. Create and submit a company registration request
        CompanyRegisterRequest req = new CompanyRegisterRequest(
            randomCompanyId,
            domain,
            "TIER_1",
            ownerEmail,
            domain
        );
        
        // Register the company and verify no exceptions are thrown
        assertDoesNotThrow(() -> comCon.registerCompany(req));
        
        // 3. Verify that a company with this ID now exists
        assertTrue(companyRepo.existsById(randomCompanyId), 
            "Company should exist in repository after registration");
        
        // Get the newly created company
        Company newCompany = companyRepo.findById(randomCompanyId).orElse(null);
        assertNotNull(newCompany, "Company should not be null");
        
        // Verify company properties
        assertEquals(randomCompanyId, newCompany.getId());
        assertEquals(domain, newCompany.getEmailDomain());
        assertEquals(ownerEmail, newCompany.getOwnerEmail());
        assertFalse(newCompany.getVerified(), "New company should not be verified");
        
        // 4. Verify a token was created for this company
        Role ownerRole = RoleManager.getRole(RoleManager.OWNER_ROLE);
        List<AppToken> tokens = tokenRepo.findByCompanyAndRole(newCompany, ownerRole);
        
        assertEquals(1, tokens.size(), "There should be exactly one owner token");
        
        // Verify token properties
        AppToken ownerToken = tokens.getFirst();
        assertEquals(newCompany.getId(), ownerToken.getCompany().getId());
        assertEquals(RoleManager.getRole(RoleManager.OWNER_ROLE).role(), ownerToken.getRole().role());
        
        // Verify that a TopLevelDomain was created
        List<TopLevelDomain> domains = topLevelDomainRepo.findByCompany(newCompany);
        assertFalse(domains.isEmpty(), "A domain should exist for the company");
        assertEquals(domain, domains.getFirst().getDomain());
    }
}
