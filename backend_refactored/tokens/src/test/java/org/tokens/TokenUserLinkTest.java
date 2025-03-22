package org.tokens;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.access.Role;
import org.access.RoleManager;
import org.assertj.core.api.Assertions;
import org.company.entities.Company;
import org.access.SubscriptionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.user.entities.AppUser;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TokenUserLinkTest {
    private final ObjectMapper om;
    private Company company;
    private Role role;
    private AppUser user;
    private AppToken token;
    
    // Initialize with properly configured ObjectMapper for date handling
    public TokenUserLinkTest() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.om = new ObjectMapper();
        this.om.setDateFormat(df);
        this.om.registerModule(new JavaTimeModule());
        this.om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    @BeforeEach
    void setUp() {
        // Create common dependencies for tests
        this.company = new Company("123", "companyName", "companyAddress", "admin@example.com", "example.com", SubscriptionManager.getSubscription("TIER_1"));
        this.role = RoleManager.getRole(RoleManager.OWNER_ROLE);
        this.user = new AppUser("admin@example.com", "admin", "password123", "firstName", "lastName", "middleName", company, role);
        this.token = new AppToken("token-123", "hashed-token-456", company, role);
        
        // Activate the token for valid link creation
        this.token.activate();
    }
    
    @Test
    void testInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Create a TokenUserLink with a random ID
        String randomId = UUID.randomUUID().toString();
        TokenUserLink link = new TokenUserLink(randomId, token, user);
        
        // Test token field
        Field tokenField = TokenUserLink.class.getDeclaredField("token");
        tokenField.setAccessible(true);
        assertEquals(token, tokenField.get(link));
        
        // Test user field
        Field userField = TokenUserLink.class.getDeclaredField("user");
        userField.setAccessible(true);
        assertEquals(user, userField.get(link));
        
        // Test linkActivationTime field (should be non-null)
        Field activationTimeField = TokenUserLink.class.getDeclaredField("linkActivationTime");
        activationTimeField.setAccessible(true);
        assertNotNull(activationTimeField.get(link));
        
        // Test linkDeactivationTime field (should be null initially)
        Field deactivationTimeField = TokenUserLink.class.getDeclaredField("linkDeactivationTime");
        deactivationTimeField.setAccessible(true);
        assertNull(deactivationTimeField.get(link));
    }
    
    @Test
    void testValidationInactiveToken() {
        // Create a token but don't activate it
        AppToken inactiveToken = new AppToken("token-inactive", "hash-inactive", company, role);
        
        // Generate a random ID
        String randomId = UUID.randomUUID().toString();
        
        // Verify that creating a link with inactive token throws IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> new TokenUserLink(randomId, inactiveToken, user));
        assertEquals("Token is not active", exception.getMessage());
    }
    
    @Test
    void testValidationRoleMismatch() {
        // Create a token with admin role
        Role adminRole = RoleManager.getRole(RoleManager.ADMIN_ROLE);
        AppToken adminToken = new AppToken("token-admin", "hash-admin", company, adminRole);
        adminToken.activate();
        
        // Generate a random ID
        String randomId = UUID.randomUUID().toString();
        
        // User has owner role from setUp, so roles don't match
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> new TokenUserLink(randomId, adminToken, user));
        assertEquals("Role of the token and the role of the user do not match", exception.getMessage());
    }
    
    @Test
    void testValidationCompanyMismatch() {
        // Create a token for a different company
        Company otherCompany = new Company("456", "otherCompany", "otherAddress", "admin@other.com", "other.com", SubscriptionManager.getSubscription("TIER_1"));
        AppToken otherCompanyToken = new AppToken("token-other", "hash-other", otherCompany, role);
        otherCompanyToken.activate();
        
        // Generate a random ID
        String randomId = UUID.randomUUID().toString();
        
        // User has different company from setUp
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> new TokenUserLink(randomId, otherCompanyToken, user));
        assertEquals("Token and user must be from the same company", exception.getMessage());
    }
    
    @Test
    void testDeactivate() throws NoSuchFieldException, IllegalAccessException {
        // Create a TokenUserLink with a random ID
        String randomId = UUID.randomUUID().toString();
        TokenUserLink link = new TokenUserLink(randomId, token, user);
        
        // Verify linkDeactivationTime is null initially
        Field deactivationTimeField = TokenUserLink.class.getDeclaredField("linkDeactivationTime");
        deactivationTimeField.setAccessible(true);
        assertNull(deactivationTimeField.get(link));
        
        // Deactivate the link
        link.deactivate();
        
        // Verify linkDeactivationTime is now set
        assertNotNull(deactivationTimeField.get(link));
        
        // Test that calling deactivate on an already deactivated link throws exception
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalStateException.class, link::deactivate);
        }
    }
    
    @Test
    void testLinkSerialization() throws JsonProcessingException {
        // Create a TokenUserLink with a random ID
        String randomId = UUID.randomUUID().toString();
        TokenUserLink link = new TokenUserLink(randomId, token, user);
        
        // Serialize the link
        String linkJson = om.writeValueAsString(link);
        
        // Parse the JSON and extract the keys
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(linkJson);
        Set<String> keys = JsonPath.read(doc, "keys()");
        
        // Verify initial serialization contains expected fields and excludes others
        List<String> expectedFields = List.of("token", "user", "linkActivationTime");
        Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        
        // Verify id and linkDeactivationTime are not present 
        assertFalse(keys.contains("id"));
        assertFalse(keys.contains("linkDeactivationTime"));
        
        // Deactivate the link and serialize again
        link.deactivate();
        linkJson = om.writeValueAsString(link);
        
        // Parse the updated JSON
        doc = Configuration.defaultConfiguration().jsonProvider().parse(linkJson);
        keys = JsonPath.read(doc, "keys()");
        
        // Verify linkDeactivationTime is now present
        expectedFields = List.of("token", "user", "linkActivationTime", "linkDeactivationTime");
        Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        assertTrue(keys.contains("linkDeactivationTime"));
    }
    
    @Test
    void testDifferentTokens() throws JsonProcessingException {
        // Create user and token with owner role
        AppUser ownerUser = new AppUser("owner@example.com", "owner", "password123", "firstName", "lastName", "middleName", company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE));
        AppToken ownerToken = new AppToken("token-owner", "hash-owner", company, 
                RoleManager.getRole(RoleManager.OWNER_ROLE));
        ownerToken.activate();
        
        // Create user and token with admin role
        AppUser adminUser = new AppUser("admin@example.com", "admin", "password123", "firstName", "lastName", "middleName", company, 
                RoleManager.getRole(RoleManager.ADMIN_ROLE));
        AppToken adminToken = new AppToken("token-admin", "hash-admin", company, 
                RoleManager.getRole(RoleManager.ADMIN_ROLE));
        adminToken.activate();
        
        // Create links with matching roles and random IDs
        String ownerLinkId = UUID.randomUUID().toString();
        String adminLinkId = UUID.randomUUID().toString();
        TokenUserLink ownerLink = new TokenUserLink(ownerLinkId, ownerToken, ownerUser);
        TokenUserLink adminLink = new TokenUserLink(adminLinkId, adminToken, adminUser);
        
        // Serialize both links
        String ownerLinkJson = om.writeValueAsString(ownerLink);
        String adminLinkJson = om.writeValueAsString(adminLink);
        
        // Parse the JSONs
        Object ownerDoc = Configuration.defaultConfiguration().jsonProvider().parse(ownerLinkJson);
        Object adminDoc = Configuration.defaultConfiguration().jsonProvider().parse(adminLinkJson);
        
        // Verify token roles are different
        String ownerTokenRole = JsonPath.read(ownerDoc, "$.token.role");
        String adminTokenRole = JsonPath.read(adminDoc, "$.token.role");

        assertEquals("OWNER", ownerTokenRole);
        assertEquals("ADMIN", adminTokenRole);
    }
}
