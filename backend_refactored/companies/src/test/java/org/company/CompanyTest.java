package org.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import org.access.Subscription;
import org.access.SubscriptionManager;
import org.assertj.core.api.Assertions;
import org.company.entities.Company;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class CompanyTest {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    void testInitialization() throws NoSuchFieldException, IllegalAccessException {
        Company company = new Company("123", "Example Company", "123 Business St", "admin@example.com", "example.com", 
                                    SubscriptionManager.getSubscription("TIER_1"));
        
        // use reflection to get the private field "serializeSensitiveCount"
        Field field = Company.class.getDeclaredField("serializeSensitiveCount");
        field.setAccessible(true);
        assertEquals(0, field.getInt(company));

        // use reflection to get the private field "subscription"
        Field field2 = Company.class.getDeclaredField("subscription");
        field2.setAccessible(true);
        assertEquals(SubscriptionManager.getSubscription("TIER_1"), field2.get(company));   

        // use reflection to get the private field "emailDomain"
        Field field3 = Company.class.getDeclaredField("emailDomain");
        field3.setAccessible(true);
        assertEquals("example.com", field3.get(company));

        // use reflection to get the private field "ownerEmail" 
        Field field4 = Company.class.getDeclaredField("ownerEmail");
        field4.setAccessible(true);
        assertEquals("admin@example.com", field4.get(company));
        
        // use reflection to get the private field "verified"
        Field field5 = Company.class.getDeclaredField("verified");
        field5.setAccessible(true);
        assertEquals(false, field5.get(company));

        // use reflection to get the private field "id"
        Field field6 = Company.class.getDeclaredField("id");
        field6.setAccessible(true);
        assertEquals("123", field6.get(company));

    }

    @Test
    void testFirstJsonSerialization() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        List<String> initialFieldsSerialization = List.of("id", "emailDomain", "ownerEmail", "verified", "subscription", "companyName", "companyAddress");
    
        String id = "aaa";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        Company com = new Company(id, "Test Company", "456 Test Ave", "admin@example.com", "example.com", sub);

        // before serializing the object; make sure none of the fields are Null
        for (Field f : com.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            assertNotNull(f.get(com));
        }

        String comJson = this.om.writeValueAsString(com);

        // after serializing the object; make sure none of the fields are Null
        for (Field f : com.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            assertNotNull(f.get(com));
        }

        // make sure the field used to condition the serialization is updated correctly
        // Otherwise, the serialization subsequently wouldn't work as intended
        Field f = Company.class.getDeclaredField("serializeSensitiveCount");
        f.setAccessible(true);
        assertEquals(2, (int) f.get(com));

        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(comJson);
        Set<String> keys = JsonPath.read(doc, "keys()");
        Assertions.assertThat(keys).hasSameElementsAs(initialFieldsSerialization);
        
        // Verify subscription is serialized as the tier string
        String subscriptionValue = JsonPath.read(doc, "$.subscription");
        assertEquals("TIER_1", subscriptionValue);
    }

    @Test
    void testCompanySubsequentSerialization1() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        List<String> serializationFields2 = List.of("subscription", "emailDomain", "verified", "companyName", "companyAddress");

        String id = "aaa";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        Company com = new Company(id, "Test Company", "456 Test Ave", "admin@example.com", "example.com", sub);

        String firstJson = this.om.writeValueAsString(com);
        
        // Verify subscription in first serialization
        Object firstDoc = Configuration.defaultConfiguration().jsonProvider().parse(firstJson);
        String firstSubscriptionValue = JsonPath.read(firstDoc, "$.subscription");
        assertEquals("TIER_1", firstSubscriptionValue);

        Field f = Company.class.getDeclaredField("serializeSensitiveCount");
        f.setAccessible(true);
        // make sure the field used to condition the serialization is updated correctly
        // Otherwise, the serialization wouldn't work
        assertEquals(2, (int) f.get(com));

        // due to the conditional serialization, any subsequent serialization
        // should return only "site" and "subscription" as keys
        // the "serializeSensitiveCount" field should not  be modified

        String comJson;
        Object doc;
        for (int i = 0; i < 100; i++) {
            comJson = this.om.writeValueAsString(com);
            assertEquals(2, (int) f.get(com));

            doc = Configuration.defaultConfiguration().jsonProvider().parse(comJson);
            Set<String> keys = JsonPath.read(doc, "keys()");
            Assertions.assertThat(keys).hasSameElementsAs(serializationFields2);
            
            // Verify subscription in subsequent serializations
            String subscriptionValue = JsonPath.read(doc, "$.subscription");
            assertEquals("TIER_1", subscriptionValue);
        }
    }

    @Test
    void testCompanySubsequentSerialization2() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        List<String> serializationFields2 = List.of("subscription", "verified", "companyName", "companyAddress");

        String id = "aaa";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");

        Company com = new Company(id, "Test Company", "456 Test Ave", "admin@example.com", null, sub);

        this.om.writeValueAsString(com);

        Field f = Company.class.getDeclaredField("serializeSensitiveCount");
        f.setAccessible(true);
        // make sure the field used to condition the serialization is updated correctly
        // Otherwise, the serialization wouldn't work
        assertEquals(2, (int) f.get(com));

        // due to the conditional serialization, any subsequent serialization
        // should return only "verified" and "subscription" as keys
        // the "serializeSensitiveCount" field should not  be modified

        String comJson;
        Object doc;
        for (int i = 0; i < 100; i++) {
            comJson = this.om.writeValueAsString(com);
            assertEquals(2, (int) f.get(com));

            doc = Configuration.defaultConfiguration().jsonProvider().parse(comJson);
            Set<String> keys = JsonPath.read(doc, "keys()");
            Assertions.assertThat(keys).hasSameElementsAs(serializationFields2);
        }
    }

    @Test
    void testVerified() {
        String id = "aaa";
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");
        Company com = new Company(id, "Test Company", "456 Test Ave", "admin@example.com", "example.com", sub);

        assertFalse(com.getVerified());

        com.verify();

        assertTrue(com.getVerified()); 

        for (int i = 0; i < 100; i++) {
            assertThrows(IllegalStateException.class, com::verify);
        }

    }

    @Test
    void testDifferentSubscriptionTiers() throws JsonProcessingException {
        // Test FREE tier
        Company freeCompany = new Company("free123", "Free Company", "789 Free St", "free@example.com", "example.com",
                                        SubscriptionManager.getSubscription("FREE"));
        String freeJson = this.om.writeValueAsString(freeCompany);
        Object freeDoc = Configuration.defaultConfiguration().jsonProvider().parse(freeJson);
        String freeSubscription = JsonPath.read(freeDoc, "$.subscription");
        assertEquals("FREE", freeSubscription);
        
        // Test TIER_1
        Company tier1Company = new Company("tier1123", "Tier 1 Company", "321 Premium Rd", "tier1@example.com", "example.com",
                                        SubscriptionManager.getSubscription("TIER_1"));
        String tier1Json = this.om.writeValueAsString(tier1Company);
        Object tier1Doc = Configuration.defaultConfiguration().jsonProvider().parse(tier1Json);
        String tier1Subscription = JsonPath.read(tier1Doc, "$.subscription");
        assertEquals("TIER_1", tier1Subscription);
        
        // Test TIER_INFINITY
        Company infinityCompany = new Company("inf123", "Infinity Company", "999 Enterprise Way", "infinity@example.com", "example.com",
                                        SubscriptionManager.getSubscription("TIER_INFINITY"));
        String infinityJson = this.om.writeValueAsString(infinityCompany);
        Object infinityDoc = Configuration.defaultConfiguration().jsonProvider().parse(infinityJson);
        String infinitySubscription = JsonPath.read(infinityDoc, "$.subscription");
        assertEquals("TIER_INFINITY", infinitySubscription);
    }
}
