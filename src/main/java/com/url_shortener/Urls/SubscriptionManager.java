package com.url_shortener.Urls;

import com.url_shortener.Service.User.UndefinedRoleException;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionManager {

    static class NoExistingSubscription extends RuntimeException{
        public NoExistingSubscription(String message) {
            super(message);
        }

    }

    private static final Map<String, Subscription> STRING_SUB_MAP = new HashMap<>();

    public static final String TIER1_SUB = "TIER_1";
    public static final String TIER_INF_SUB = "TIER_INFINITY";

    static {
        STRING_SUB_MAP.put(TIER1_SUB.toLowerCase(), TierOne.getInstance());
        STRING_SUB_MAP.put(TIER_INF_SUB.toLowerCase(), TierInfinity.getInstance());
    }

    public static Subscription getSubscription(String subStr) throws UndefinedRoleException {

        if (!STRING_SUB_MAP.containsKey(subStr.toLowerCase())) {
            return null;
        }
        return STRING_SUB_MAP.get(subStr.toLowerCase());
    }
}
