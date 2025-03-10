package org.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.access.Role;
import org.access.RoleManager;
import org.assertj.core.api.Assertions;
import org.company.entities.Company;
import org.access.SubscriptionManager;
import org.junit.jupiter.api.Test;
import org.user.entities.AppUser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {
    private final ObjectMapper om = new ObjectMapper();

    @Test
    void testInitialization() throws NoSuchFieldException, IllegalAccessException {
        // Create necessary dependencies
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "example.com", "admin@example.com");
        Role role = RoleManager.getRole(RoleManager.OWNER_ROLE);
        
        // Create the user
        AppUser user = new AppUser("user@example.com", "testuser", "password123", company, role);
        
        // Test email field
        Field emailField = AppUser.class.getDeclaredField("email");
        emailField.setAccessible(true);
        assertEquals("user@example.com", emailField.get(user));
        
        // Test username field
        Field usernameField = AppUser.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        assertEquals("testuser", usernameField.get(user));
        
        // Test password field
        Field passwordField = AppUser.class.getDeclaredField("password");
        passwordField.setAccessible(true);
        assertEquals("password123", passwordField.get(user));
        
        // Test company field
        Field companyField = AppUser.class.getDeclaredField("company");
        companyField.setAccessible(true);
        assertEquals(company, companyField.get(user));
        
        // Test role field
        Field roleField = AppUser.class.getDeclaredField("role");
        roleField.setAccessible(true);
        assertEquals(role, roleField.get(user));
    }

    @Test
    void testJsonSerialization() throws JsonProcessingException {
        // Create necessary dependencies
        Company company = new Company("123", SubscriptionManager.getSubscription("TIER_1"), "example.com", "admin@example.com");
        Role role = RoleManager.getRole(RoleManager.OWNER_ROLE);
        
        // Create the user
        AppUser user = new AppUser("user@example.com", "testuser", "password123", company, role);
        
        // Serialize the user
        String userJson = this.om.writeValueAsString(user);
        
        // Parse the JSON and extract the keys
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(userJson);
        Set<String> keys = JsonPath.read(doc, "keys()");
        
        // Verify serialized fields
        List<String> expectedFields = List.of("email", "username", "company", "role");
        Assertions.assertThat(keys).hasSameElementsAs(expectedFields);
        
        // Verify password is NOT included (due to @JsonProperty annotation)
        assertFalse(keys.contains("password"));
        
        // Verify role is serialized as a string
        String serializedRole = JsonPath.read(doc, "$.role");
        assertEquals(RoleManager.OWNER_ROLE, serializedRole);
    }


} 