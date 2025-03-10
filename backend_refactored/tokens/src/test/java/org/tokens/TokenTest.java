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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TokenTest {
    private final ObjectMapper om;
    private Company company;
    private Role role;
    
    // Initialize the class with a properly configured ObjectMapper for date handling
    public TokenTest() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        this.om = new ObjectMapper();
        this.om.setDateFormat(df);
        this.om.registerModule(new JavaTimeModule());
        this.om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    @BeforeEach
    void setUp() {
        // Create common dependencies for tests
        this.company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "example.com", "admin@example.com");
        this.role = RoleManager.getRole(RoleManager.OWNER_ROLE);
    }
    
    @Test
    void testInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Create a token
        String tokenId = "token-123";
        String tokenHash = "hashed-token-456";
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        
        AppToken token = new AppToken(tokenId, tokenHash, company, role, expirationTime);
        
        // Test tokenId field
        Field tokenIdField = AppToken.class.getDeclaredField("tokenId");
        tokenIdField.setAccessible(true);
        assertEquals(tokenId, tokenIdField.get(token));
        
        // Test tokenHash field
        Field tokenHashField = AppToken.class.getDeclaredField("tokenHash");
        tokenHashField.setAccessible(true);
        assertEquals(tokenHash, tokenHashField.get(token));
        
        // Test tokenState field (should be INACTIVE by default)
        Field tokenStateField = AppToken.class.getDeclaredField("tokenState");
        tokenStateField.setAccessible(true);
        assertEquals(AppToken.TokenState.INACTIVE, tokenStateField.get(token));
        
        // Test expirationTime field
        Field expirationTimeField = AppToken.class.getDeclaredField("expirationTime");
        expirationTimeField.setAccessible(true);
        assertEquals(expirationTime, expirationTimeField.get(token));
        
        // Test company field
        Field companyField = AppToken.class.getDeclaredField("company");
        companyField.setAccessible(true);
        assertEquals(company, companyField.get(token));
        
        // Test role field
        Field roleField = AppToken.class.getDeclaredField("role");
        roleField.setAccessible(true);
        assertEquals(role, roleField.get(token));
        
        // Test alternate constructor without expiration time
        AppToken token2 = new AppToken(tokenId, tokenHash, company, role);
        expirationTimeField.setAccessible(true);
        assertNull(expirationTimeField.get(token2));
    }
    
    @Test
    void testTokenSerialization() throws JsonProcessingException {
        // Create a token
        String tokenId = "token-123";
        String tokenHash = "hashed-token-456";
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        
        AppToken token = new AppToken(tokenId, tokenHash, company, role, expirationTime);
        
        // Serialize the token
        String tokenJson = this.om.writeValueAsString(token);
        
        // Parse the JSON and extract the keys
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(tokenJson);
        Set<String> keys = JsonPath.read(doc, "keys()");
        
        // Verify serialized fields (tokenId and tokenHash should be excluded due to @JsonProperty)
        List<String> expectedFields = List.of("tokenState", "expirationTime", "company", "role");
        Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        
        // Verify tokenId and tokenHash are NOT included (due to @JsonProperty annotation)
        assertFalse(keys.contains("tokenId"));
        assertFalse(keys.contains("tokenHash"));
        
        // Verify tokenState value
        String tokenState = JsonPath.read(doc, "$.tokenState");
        assertEquals("INACTIVE", tokenState);
    }
    
    @Test
    void testActivateToken() throws NoSuchFieldException, IllegalAccessException {
        // Create a token
        AppToken token = new AppToken("token-123", "hashed-token-456", company, role);
        
        // Verify initial state is INACTIVE
        Field tokenStateField = AppToken.class.getDeclaredField("tokenState");
        tokenStateField.setAccessible(true);
        assertEquals(AppToken.TokenState.INACTIVE, tokenStateField.get(token));
        
        // Activate the token
        token.activate();
        
        // Verify state is now ACTIVE
        assertEquals(AppToken.TokenState.ACTIVE, tokenStateField.get(token));
        
        // Test that activating an already active token throws exception
        for (int i = 0; i < 10; i++) {
            assertThrows(IllegalStateException.class, token::activate);
        }
    }
    
    @Test
    void testExpireToken() throws NoSuchFieldException, IllegalAccessException {
        // Create a token
        AppToken token = new AppToken("token-123", "hashed-token-456", company, role);
        
        // Verify initial state is INACTIVE
        Field tokenStateField = AppToken.class.getDeclaredField("tokenState");
        tokenStateField.setAccessible(true);
        assertEquals(AppToken.TokenState.INACTIVE, tokenStateField.get(token));
        
        // Expire the token
        token.expire();
        
        // Verify state is now EXPIRED
        assertEquals(AppToken.TokenState.EXPIRED, tokenStateField.get(token));
        
        // Test that expiring an already expired token throws exception
        assertThrows(IllegalStateException.class, token::expire);
        
        // Create another token
        AppToken token2 = new AppToken("token-789", "hashed-token-012", company, role);
        
        // Activate it first
        token2.activate();
        assertEquals(AppToken.TokenState.ACTIVE, tokenStateField.get(token2));
        
        // Then expire it
        token2.expire();
        assertEquals(AppToken.TokenState.EXPIRED, tokenStateField.get(token2));
    }
    
    @Test
    void testTokenStateTransitions() {
        // Create a token
        AppToken token = new AppToken("token-123", "hashed-token-456", company, role);
        
        // Test valid state transitions
        assertEquals(AppToken.TokenState.INACTIVE, token.getTokenState());
        token.activate();
        assertEquals(AppToken.TokenState.ACTIVE, token.getTokenState());
        token.expire();
        assertEquals(AppToken.TokenState.EXPIRED, token.getTokenState());
        
        // Test invalid state transitions
        AppToken inactiveToken = new AppToken("token-789", "hashed-token-012", company, role);
        inactiveToken.expire();
        assertEquals(AppToken.TokenState.EXPIRED, inactiveToken.getTokenState());
        assertThrows(IllegalStateException.class, inactiveToken::activate);
    }
    
    @Test
    void testTokenWithExpiration() throws JsonProcessingException {
        // Create a token with expiration
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        AppToken token = new AppToken("token-123", "hashed-token-456", company, role, expirationTime);
        
        // Serialize the token
        String tokenJson = this.om.writeValueAsString(token);
        
        // Parse the JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(tokenJson);
        
        // Verify expirationTime is included
        assertTrue(JsonPath.read(doc, "$.expirationTime") != null);
        
        // Create a token without expiration
        AppToken token2 = new AppToken("token-789", "hashed-token-012", company, role);
        
        // Serialize the token
        String token2Json = this.om.writeValueAsString(token2);
        
        // Parse the JSON
        Object doc2 = Configuration.defaultConfiguration().jsonProvider().parse(token2Json);
        
        // Verify fields - note that with NON_NULL policy, expirationTime should be omitted
        Set<String> keys2 = JsonPath.read(doc2, "keys()");
        assertFalse(keys2.contains("expirationTime"));
    }
}
