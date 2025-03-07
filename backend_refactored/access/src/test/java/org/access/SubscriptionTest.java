package org.access;

import org.url.UrlEntity;
import org.junit.jupiter.api.Test;
import org.utils.CustomGenerator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionTest {
    private final CustomGenerator gen = new CustomGenerator();

    @Test
    void subscriptionWorksWithStringIgnoreCase() {
        // make sure the managed returns the correct role for any version of the input string
        List<String> subs = List.of("TIER_INFINITY", "TIER_1");

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
        List<String> subs = List.of("TIER_INFINITY", "TIER_1");
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
    void throwsCorrectSubViolationError() {

        UrlEntity urlEntity1 = UrlEntity.LEVEL_NAME;
        assertThrows(SubscriptionManager.LevelNamesSubExceeded.class,
                () -> SubscriptionManager.throwSubscriptionViolatedException(urlEntity1, 0, 0, 0));

        UrlEntity urlEntity2 = UrlEntity.PATH_VARIABLE;
        assertThrows(SubscriptionManager.LevelPathVariablesSubExceeded.class,
                () -> SubscriptionManager.throwSubscriptionViolatedException(urlEntity2, 0, 0, 0));

        UrlEntity urlEntity3 = UrlEntity.QUERY_PARAM;
        assertThrows(SubscriptionManager.QueryParametersSubExceeded.class,
                () -> SubscriptionManager.throwSubscriptionViolatedException(urlEntity3, 0, 0, 0));

        UrlEntity urlEntity4 = UrlEntity.QUERY_PARAM_VALUE;
        assertThrows(SubscriptionManager.QueryParametersValuesSubExceeded.class,
                () -> SubscriptionManager.throwSubscriptionViolatedException(urlEntity4, 0, 0, 0));
    }

    @Test
    void testTierOneSingletonObj() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_1");
        for (int i = 0; i <= 10; i++) {
            Subscription anotherSub = SubscriptionManager.getSubscription("TIER_1");
            assertSame(sub, anotherSub); // make sure to use assertSame rather than assertEquals...
        }
    }

    @Test
    void testInfOneSingletonObj() {
        Subscription sub = SubscriptionManager.getSubscription("TIER_INFINITY");
        for (int i = 0; i <= 10; i++) {
            Subscription anotherSub = SubscriptionManager.getSubscription("TIER_INFINITY");
            assertSame(sub, anotherSub); // make sure to use assertSame rather than assertEquals...
        }
    }
}
