package org.api.unitsTests.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.api.controllers.company.CompanyController;
import org.api.exceptions.CompanyExceptions;
import org.api.internal.StubCompanyRepo;
import org.api.internal.StubCounterRepo;
import org.api.internal.StubUserRepo;
import org.api.requests.CompanyRegisterRequest;
import org.common.Subscription;
import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.data.entities.CompanyWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class CompanyControllerTest {

    private final StubCompanyRepo companyRepo;
    private final StubCounterRepo counterRepo;
    private final CustomGenerator gen = new CustomGenerator();
    private final CustomGenerator mockGen;
    private final CompanyController comCon;
    public static int COUNTER = 0;

    @BeforeEach()
    public void setCounter() {
        COUNTER = 0;
    }

    private CustomGenerator setMockCG() {
        CustomGenerator mockedCG = spy();
        when(mockedCG.randomString(anyInt())).then(
          invocation -> {CompanyControllerTest.COUNTER += 1; return "RANDOM_STRING_" + CompanyControllerTest.COUNTER;}
        );
        return mockedCG;
    }

    // use mockito to fake the random generation of Tokens
    public CompanyControllerTest() throws NoSuchFieldException, IllegalAccessException {
        this.companyRepo = new StubCompanyRepo();
        this.mockGen = setMockCG();
        this.counterRepo = new StubCounterRepo();
        // set a stubCustomGenerator, so we can verify the registerCompany method properly
        this.comCon = new CompanyController(this.companyRepo,
                new StubUserRepo(this.companyRepo),
                this.counterRepo,
                this.mockGen);
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
    void testRegisterErrorExistingCompany() {
        for (CompanyWrapper w: this.companyRepo.getWrappers()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(w.getId(),
                    w.getSite(),
                    w.getSubscription().getTier());

            Assertions.assertThrows(
                    CompanyExceptions.ExistingCompanyException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }
    }

    @Test
    void testRegisterUniquenessConstraints() {
        for (CompanyWrapper w: this.companyRepo.getWrappers()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(this.gen.randomString(10),
                    w.getSite(),
                    w.getSubscription().getTier());

            Assertions.assertThrows(
                    CompanyExceptions.ExistingSiteException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }
    }

    @Test
    void testCompanyCount() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = this.comCon.getClass().getDeclaredMethod("getCompanyCount");
        method.setAccessible(true);

        for (int i = 0; i < 100; i++) {
            long count = (long) method.invoke(this.comCon);
            assertEquals(count, i);
            assertEquals(i + 1, this.counterRepo.findById(Company.COMPANY_COLLECTION_NAME).get().getCount());
        }

        // make sure to delete all the
        this.counterRepo.deleteAll();
    }

    @Test
    void testRegisterValidCompany() throws JsonProcessingException, IllegalAccessException {
        List<String> initialFieldsSerialization = List.of("id",
                "site",
                "siteHash",
                "roleTokens",
                "roleTokensHashed",
                "subscription");

        String id = "a_company_id";
        String site = "www.completelyNewSite.com";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        CompanyRegisterRequest req = new CompanyRegisterRequest(id, site, sub.getTier());

        ResponseEntity<String> res = comCon.registerCompany(req);

        // the new company object must have been added to the database
        assertEquals(3, this.companyRepo.getDb().size());

        Company newC = this.companyRepo.getDb().get(2);

        // use reflection to make sure none of the fields are none
        List<Field> fields = List.of(Company.class.getDeclaredFields());
        for (Field f : fields) {
            f.setAccessible(true);
            assertNotNull(f.get(newC));
        }

        String body = res.getBody();

        // read the body to make sure the company record is saved correctly
        Object doc;
        doc = Configuration.defaultConfiguration().jsonProvider().parse(body);
        Set<String> keys = JsonPath.read(doc, "keys()");

        org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(initialFieldsSerialization);
        // the next step is to make sure everything is saved and serialized correctly

        // id
        String serializedId = JsonPath.read(doc, "$.id");
        assertEquals(id, serializedId);

        // the site
        String serializedSite = JsonPath.read(doc, "$.site");
        assertEquals(site, serializedSite);

        // site hash
        String siteHashSerialized = JsonPath.read(doc, "$.siteHash");
        assertEquals(this.gen.generateId(0), siteHashSerialized);

        // tokens
        Map<String, String> tokens = JsonPath.read(doc, "$.roleTokens");
        Map<String, String> expectedTokens = Map.of("owner", "RANDOM_STRING_1",
                "admin", "RANDOM_STRING_2",
                "registereduser", "RANDOM_STRING_3");
        assertEquals(expectedTokens, tokens);

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        Map<String, String> hashedTokens = JsonPath.read(doc, "$.roleTokensHashed");

        assertTrue(encoder.matches("RANDOM_STRING_1", hashedTokens.get("owner")));
        assertTrue(encoder.matches("RANDOM_STRING_2", hashedTokens.get("admin")));
        assertTrue(encoder.matches("RANDOM_STRING_3", hashedTokens.get("registereduser")));

        // subscription
        String subSerializer = JsonPath.read(doc, "$.subscription");
        assertEquals(subSerializer, sub.getTier());
    }


    //////////////////////// view company details  ////////////////////////
    @Test
    void testViewNoExistingCompany() {
        for (int i = 0; i <= 100; i++) {
            String NoId = this.gen.randomString(100);
            assertThrows(CompanyExceptions.NoCompanyException.class, () -> this.comCon.viewCompanyDetails(NoId));
        }
    }

    @Test
    void testViewCompanyDetails() throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {
        List<String> fields = List.of("site",
                "subscription");

        List<Company> db = this.companyRepo.getDb();

        Field fid = Company.class.getDeclaredField("id");
        fid.setAccessible(true);

        Field fSite = Company.class.getDeclaredField("site");
        fSite.setAccessible(true);

        Field fSub = Company.class.getDeclaredField("subscription");
        fSub.setAccessible(true);


        for (Company c : db) {
            // access the company id
            String id = (String) fid.get(c);
            assertDoesNotThrow(() -> comCon.viewCompanyDetails(id));

            ResponseEntity<String> re = comCon.viewCompanyDetails(id);
            // check the status code
            assertEquals(HttpStatus.OK, re.getStatusCode());
            String body = re.getBody();

            Object doc = Configuration.defaultConfiguration().jsonProvider().parse(body);
            Set<String> keys = JsonPath.read(doc, "keys()");

            // make sure the keys are correct
            org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(fields);

            String serializedSite = JsonPath.read(doc, "$.site");
            String serializedSub = JsonPath.read(doc, "$.subscription");

            // make sure the values are correct
            assertEquals(((Subscription) fSub.get(c)).getTier(), serializedSub);
            assertEquals(fSite.get(c), serializedSite);
        }
    }

    //////////////////////// delete a company  ////////////////////////


}
