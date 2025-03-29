package org.access;

import org.junit.jupiter.api.Test;
import org.utils.CustomGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionTest {
    private final CustomGenerator gen = new CustomGenerator();

    @Test
    void subscriptionWorksWithStringIgnoreCase() {
        // make sure the managed returns the correct role for any version of the input string
        List<String> subs = List.of("TIER_INFINITY", "TIER_1", "FREE");

        for (String subStr: subs) {
            String s;
            for (int i = 0; i <= 10; i++) {
                s = gen.randomCaseString(subStr);
                assertEquals(SubscriptionManager.getSubscription(s).getTier(), subStr);
            }
        }
    }

    @Test
    void throwsErrorForInvalidString(){
        // make sure the managed returns the correct role for any version of the input string
        List<String> subs = List.of("TIER_INFINITY", "TIER_1", "FREE");
        String randomStr;
        for (String subStr: subs) {
            for (int i = 0; i <= 100; i++) {
                randomStr = gen.randomAlphaString(subStr.length());
                // a final copy of the variable to be used in the test lambda expression
                String finalRandomStr = randomStr;
                assertThrows(SubscriptionManager.NoExistingSubscription.class,
                        () -> SubscriptionManager.getSubscription(finalRandomStr));
            }
        }
    }


    @Test
    void testTierOneSingletonObj() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");
        for (int i = 0; i <= 10; i++) {
            Subscription anotherSub = SubscriptionManager.getSubscription("TIER_1");
            assertSame(sub, anotherSub);
        }
    }

    @Test
    void testFreeSingletonObj() {
        Subscription sub = SubscriptionManager.getSubscription("FREE");
        for (int i = 0; i <= 10; i++) {
            Subscription anotherSub = SubscriptionManager.getSubscription("FREE");
            assertSame(sub, anotherSub);
        }
    }

    @Test
    void testInfOneSingletonObj() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_INFINITY");
        for (int i = 0; i <= 10; i++) {
            Subscription anotherSub = SubscriptionManager.getSubscription("TIER_INFINITY");
            assertSame(sub, anotherSub);
        }
    }

    @Test
    void testFreeLimits() {
        Subscription sub = SubscriptionManager.getSubscription("FREE");
        assertEquals(5, sub.getMaxNumLevels());
        assertEquals(1, sub.getMaxAdmins());
        assertEquals(2, sub.getMaxEmployees());
        assertEquals(0, sub.getMaxHistorySize());
        assertEquals(20, sub.getEncodingDailyLimit());
        assertEquals(40, sub.getMinUrlLength());
        assertEquals(15, sub.getMinParameterLength());
        assertEquals(20, sub.getMinVariableLength());
    }

    @Test
    void testTierOneLimits() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");
        assertEquals(10, sub.getMaxNumLevels());
        assertEquals(2, sub.getMaxAdmins());
        assertEquals(6, sub.getMaxEmployees());
        assertEquals(10, sub.getMaxHistorySize());
        assertEquals(100, sub.getEncodingDailyLimit());
        assertEquals(25, sub.getMinUrlLength());
        assertEquals(15, sub.getMinParameterLength());
        assertEquals(15, sub.getMinVariableLength());
    }

    @Test
    void testTierInfinityLimits() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_INFINITY");
        assertNull(sub.getMaxNumLevels());
        assertEquals(3, sub.getMaxAdmins());
        assertEquals(10, sub.getMaxEmployees());
        assertEquals(100, sub.getMaxHistorySize());
        assertNull(sub.getEncodingDailyLimit());
        assertEquals(0, sub.getMinUrlLength());
        assertEquals(5, sub.getMinParameterLength());
        assertEquals(5, sub.getMinVariableLength());
    }


    @Test
    void testSubscriptionSerialization() throws JsonProcessingException {
        // Create an ObjectMapper for serialization
        ObjectMapper om = new ObjectMapper();
        
        // Test serialization of each subscription type
        FreeTier freeSub = (FreeTier) SubscriptionManager.getSubscription("FREE");
        TierOne tier1Sub = (TierOne) SubscriptionManager.getSubscription("TIER_1");
        TierInfinity infinitySub = (TierInfinity) SubscriptionManager.getSubscription("TIER_INFINITY");
        
        // Verify FREE subscription serializes to just the string "FREE"
        String freeJson = om.writeValueAsString(freeSub);
        assertEquals("\"FREE\"", freeJson);
        
        // Verify TIER_1 subscription serializes to just the string "TIER_1"
        String tier1Json = om.writeValueAsString(tier1Sub);
        assertEquals("\"TIER_1\"", tier1Json);
        
        // Verify TIER_INFINITY subscription serializes to just the string "TIER_INFINITY"
        String infinityJson = om.writeValueAsString(infinitySub);
        assertEquals("\"TIER_INFINITY\"", infinityJson);
    
        // deserialization might require a deeper dive into Jackson
    }
}
