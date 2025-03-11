package org.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.access.SubscriptionManager;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import static org.junit.jupiter.api.Assertions.*;


class DomainTest {
    private final ObjectMapper om;
    
    // Initialize the class with a custom ObjectMapper for date formatting
    public DomainTest() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.om = new ObjectMapper();
        this.om.setDateFormat(df);
        this.om.registerModule(new JavaTimeModule());
        this.om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Create a company for the domain
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "test@example.com", "example.com");
        
        // Create the TopLevelDomain with test values
        TopLevelDomain domain = new TopLevelDomain("456", "example.com", "some_hash", company);
        
        // Test id field
        Field idField = TopLevelDomain.class.getDeclaredField("id");
        idField.setAccessible(true);
        assertEquals("456", idField.get(domain));
        
        // Test domain field
        Field domainField = TopLevelDomain.class.getDeclaredField("domain");
        domainField.setAccessible(true);
        assertEquals("example.com", domainField.get(domain));
        
        // Test hashedDomain field
        Field hashedDomainField = TopLevelDomain.class.getDeclaredField("hashedDomain");
        hashedDomainField.setAccessible(true);
        assertEquals("some_hash", hashedDomainField.get(domain));
        
        // Test company field
        Field companyField = TopLevelDomain.class.getDeclaredField("company");
        companyField.setAccessible(true);
        assertEquals(company, companyField.get(domain));
        
        // Test activationTime field (non-null)
        Field activationTimeField = TopLevelDomain.class.getDeclaredField("activationTime");
        activationTimeField.setAccessible(true);
        assertNotNull(activationTimeField.get(domain));
        
        // Test deactivationTime field (should be null initially)
        Field deactivationTimeField = TopLevelDomain.class.getDeclaredField("deactivationTime");
        deactivationTimeField.setAccessible(true);
        assertNull(deactivationTimeField.get(domain));
        
        // Test domainState field
        Field domainStateField = TopLevelDomain.class.getDeclaredField("domainState");
        domainStateField.setAccessible(true);
        assertEquals(TopLevelDomain.DomainState.ACTIVE, domainStateField.get(domain));
    }

    @Test
    void testDeactivate() throws NoSuchFieldException, IllegalAccessException {
        // Create a company for the domain
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "test@example.com", "example.com");
        
        // Create the TopLevelDomain
        TopLevelDomain domain = new TopLevelDomain("456", "example.com", "some_hash", company);
        
        // Verify initial state is ACTIVE
        Field domainStateField = TopLevelDomain.class.getDeclaredField("domainState");
        domainStateField.setAccessible(true);
        assertEquals(TopLevelDomain.DomainState.ACTIVE, domainStateField.get(domain));
        
        // Verify deactivationTime is null initially
        Field deactivationTimeField = TopLevelDomain.class.getDeclaredField("deactivationTime");
        deactivationTimeField.setAccessible(true);
        assertNull(deactivationTimeField.get(domain));
        
        // Deactivate the domain
        domain.deactivate();
        
        // Verify state changed to INACTIVE
        assertEquals(TopLevelDomain.DomainState.INACTIVE, domainStateField.get(domain));
        
        // Verify deactivationTime is now set
        assertNotNull(deactivationTimeField.get(domain));
        
        // Test that calling deactivate on an inactive domain throws exception
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalStateException.class, domain::deactivate);
        }
    }

    @Test
    void testDeprecate() throws NoSuchFieldException, IllegalAccessException {
        // Create a company for the domain
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "test@example.com", "example.com");
        
        // Create the TopLevelDomain
        TopLevelDomain domain = new TopLevelDomain("456", "example.com", "some_hash", company);
        
        // Verify initial state is ACTIVE
        Field domainStateField = TopLevelDomain.class.getDeclaredField("domainState");
        domainStateField.setAccessible(true);
        assertEquals(TopLevelDomain.DomainState.ACTIVE, domainStateField.get(domain));
        
        // Deprecate the domain
        domain.deprecate();
        
        // Verify state changed to DEPRECATED
        assertEquals(TopLevelDomain.DomainState.DEPRECATED, domainStateField.get(domain));
        
        // Verify deactivationTime is now set
        Field deactivationTimeField = TopLevelDomain.class.getDeclaredField("deactivationTime");
        deactivationTimeField.setAccessible(true);
        assertNotNull(deactivationTimeField.get(domain));
    
        // Test that calling deprecate on an already deprecated domain throws exception
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalStateException.class, domain::deprecate);
        }
        
        // Additional test: can deactivate a deprecated domain?
        assertThrows(IllegalStateException.class, domain::deactivate);
    }

    @Test
    void testDomainSerialization() throws JsonProcessingException {
        // Create dependencies
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "test@example.com", "example.com");
        
        // Create the TopLevelDomain with test values
        TopLevelDomain domain = new TopLevelDomain("456", "example.com", "some_hash", company);
        
        // Serialize the domain
        String domainJson = om.writeValueAsString(domain);
        
        // Parse the JSON and extract the keys
        Object doc = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(domainJson);
        java.util.Set<String> keys = com.jayway.jsonpath.JsonPath.read(doc, "keys()");
        
        // 1. Verify initial serialization contains expected fields and excludes others
        java.util.List<String> expectedFields = java.util.List.of("domain", "activationTime", "domainState", "company");
        org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        
        // Verify id and hashedDomain are not present (due to @JsonProperty annotations)
        assertFalse(keys.contains("id"));
        assertFalse(keys.contains("hashedDomain"));
        assertFalse(keys.contains("deactivationTime"));
        
        // 2. Deactivate the domain and serialize again
        domain.deactivate();
        domainJson = om.writeValueAsString(domain);
        
        // Parse the updated JSON
        doc = com.jayway.jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(domainJson);
        keys = com.jayway.jsonpath.JsonPath.read(doc, "keys()");
        
        // Verify deactivationTime is now present
        expectedFields = java.util.List.of("domain", "activationTime", "deactivationTime", "domainState", "company");
        org.assertj.core.api.Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        assertTrue(keys.contains("deactivationTime"));
        
        // Verify that the domainState value is correct
        String state = com.jayway.jsonpath.JsonPath.read(doc, "$.domainState");
        assertEquals("INACTIVE", state);
    }
}
