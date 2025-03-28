package org.access;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionManager {

    public static abstract class SubscriptionViolatedException extends RuntimeException{
        public SubscriptionViolatedException(String message) {
            super(message);
        }
    }

    public static class MaxNumLevelsSubExceeded extends SubscriptionViolatedException {

        public MaxNumLevelsSubExceeded(String message) {
            super(message);
        }

        public MaxNumLevelsSubExceeded(int numLevels, int maxNumLevels) {
            super("The number of levels " + numLevels + " exceeds the subscription limit " + maxNumLevels);
        }
    }


    // public static void throwSubscriptionViolatedException(UrlEntity valueType,
    //                                                       int level,
    //                                                       int num,
    //                                                       int max) {
    //     switch (valueType) {
    //         case UrlEntity.LEVEL_NAME -> throw new LevelNamesSubExceeded(level, num, max);
    //         case UrlEntity.PATH_VARIABLE -> throw new LevelPathVariablesSubExceeded(level, num, max);
    //         case UrlEntity.QUERY_PARAM_VALUE -> throw new QueryParametersValuesSubExceeded(level, num, max);
    //         default -> throw new QueryParametersSubExceeded(level, num, max);
    //     }
    // }

    // make the class public to be accessed anywhere in the codebase
    public static class NoExistingSubscription extends RuntimeException {
        public NoExistingSubscription(String message) {
            super(message);
        }
    }

    private static final Map<String, Subscription> STRING_SUB_MAP = new HashMap<>();
    public static final String TIER1_SUB = "TIER_1";
    public static final String TIER_INF_SUB = "TIER_INFINITY";
    public static final String FREE_SUB  = "FREE";

    static {
        STRING_SUB_MAP.put(FREE_SUB.toLowerCase(), FreeTier.getInstance());
        STRING_SUB_MAP.put(TIER1_SUB.toLowerCase(), TierOne.getInstance());
        STRING_SUB_MAP.put(TIER_INF_SUB.toLowerCase(), TierInfinity.getInstance());
    }

    public static Subscription getSubscription(String subStr) throws NoExistingSubscription {

        if (!STRING_SUB_MAP.containsKey(subStr.toLowerCase())) {
            throw new NoExistingSubscription("the requested subscription " +  subStr + " is not yet supported");
        }
        return STRING_SUB_MAP.get(subStr.toLowerCase());
    }
}
