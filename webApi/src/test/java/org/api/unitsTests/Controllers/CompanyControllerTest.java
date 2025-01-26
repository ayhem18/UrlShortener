package org.api.unitsTests.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.api.controllers.company.CompanyController;
import org.api.exceptions.CompanyUniquenessConstraints;
import org.api.internal.StubCompanyRepo;
import org.api.internal.StubUserRepo;
import org.api.requests.CompanyRegisterRequest;
import org.common.Subscription;
import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.utils.CustomGenerator;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompanyControllerTest {

    private final StubCompanyRepo cRepo;
    private final CustomGenerator gen = new CustomGenerator();
    private final CustomGenerator mockGen;
    private final CompanyController comCon;
    public static int COUNTER = 0;

    @BeforeEach()
    public void setCounter() {
        COUNTER = 0;
    }

    private CustomGenerator setMockCG() {
        CustomGenerator mockedCG = mock();
        when(mockedCG.randomString(anyInt())).then(
          invocation -> {CompanyControllerTest.COUNTER += 1; return "RANDOM_STRING_" + CompanyControllerTest.COUNTER;}
        );
        return mockedCG;
    }

    // use mockito to fake the random generation of Tokens
    public CompanyControllerTest() {
        this.cRepo = new StubCompanyRepo();
        this.mockGen = setMockCG();
        // set a stubCustomGenerator, so we can verify the registerCompany method properly
        this.comCon = new CompanyController(this.cRepo, new StubUserRepo(cRepo), this.mockGen);
    }

    @Test
    public void testMockCG() {
        // a small test to make sure the mock object works as expected
        for (int i = 0; i <= 100; i++) {
            Assertions.assertEquals(this.mockGen.randomString(10), "RANDOM_STRING_" + (i + 1));
        }
    }

    @Test
    void testRegisterErrorExistingCompany() {
        for (Company c: this.cRepo.getDb()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(c.getId(),
                    c.getSite(),
                    c.getSubscription().getTier());

            Assertions.assertThrows(
                    CompanyUniquenessConstraints.ExistingCompanyException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }
    }

    @Test
    void testRegisterUniquenessConstraints() {
        for (Company c: this.cRepo.getDb()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(this.gen.randomString(10), // new id
                    c.getSite(), // but non-unique site...
                    c.getSubscription().getTier());

            Assertions.assertThrows(
                    CompanyUniquenessConstraints.ExistingSiteException.class,
                    () -> this.comCon.registerCompany(req)
            );
        }
    }

    @Test
    void testRegisterValidCompany() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        List<String> initialFieldsSerialization = List.of("id",
                "site",
                "siteId",
                "roleTokens",
                "roleTokensHashed",
                "subscription");

        String id = this.gen.randomString(10);
        String site = "www.completelyNewSite.com";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        Map<String, String> tokens = Map.of("owner", "RANDOM_STRING_1",
                "admin", "RANDOM_STRING_2",
                "registereduser", "RANDOM_STRING_3");

        CompanyRegisterRequest req = new CompanyRegisterRequest(id, site, sub.getTier());

        ResponseEntity<String> res = comCon.registerCompany(req);

        // the new company object must have been added to the database
        assertEquals(3, this.cRepo.getDb().size());

        this.cRepo.getDb()

        Company newC = this.cRepo.getDb().get(2);

        // use reflection to make sure none of the fields are none
//        List<Field> fields = List.of(Company.class.getDeclaredFields());
//        for (Field f : fields) {
//            f.setAccessible(true);
//            assertNotNull(f.get(newC));
//        }

        Field f1 = Company.class.getDeclaredField("site");
        f1.setAccessible(true);
        assertNotNull(f1.get(newC), "The 'site' field is null");

        Field f2 = Company.class.getDeclaredField("serializeSensitiveCount");
        f2.setAccessible(true);
        assertEquals(4, (int) f2.get(newC), "the serializedSensitiveCount field is tripping");

        Field f = Company.class.getDeclaredField("siteId");
        f.setAccessible(true);
        assertNotNull(f.get(newC), "The siteId field is null");

        String body = res.getBody();

        // read the body to make sure everything is registered correctly

        Object doc;
        doc = Configuration.defaultConfiguration().jsonProvider().parse(body);
        Set<String> keys = JsonPath.read(doc, "keys()");

        org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(initialFieldsSerialization);

        // the next step is to make sure everything is saved and serialized correctly
        // id
        String serializedId = JsonPath.read(doc, ".id");
        assertEquals(id, serializedId);

    }
}
