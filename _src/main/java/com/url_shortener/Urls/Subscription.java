package com.url_shortener.Urls;


public interface Subscription {

    String getTier();

    Integer getMaxNumLevels();

    Integer getMaxLevelNames();

    Integer getMaxPathVariables();

    Integer getMaxQueryParameters();

    Integer getMaxQueryValues();

    default Integer get(UrlEntity valueType) {
        return switch (valueType) {
            case UrlEntity.LEVEL_NAME -> this.getMaxLevelNames();
            case UrlEntity.PATH_VARIABLE -> this.getMaxPathVariables();
            case UrlEntity.QUERY_PARAM_VALUE -> this.getMaxQueryValues();
            default -> this.getMaxQueryParameters();
        };
    }
}


// each tier class will be implemented using the Singleton Design Pattern

class TierOne implements Subscription {

    private static TierOne singleton;

    public static TierOne getInstance() {
        if (singleton == null) {
            singleton = new TierOne();
        }
        return singleton;
    }

    private TierOne() {};


    @Override
    public String getTier() {
        return "TIER_1";
    }

    // all methods below return "3" just to ease testing !!

    @Override
    public Integer getMaxNumLevels() {
        return 3;
    }

    @Override
    public Integer getMaxLevelNames() {
        return 3;
    }

    @Override
    public Integer getMaxPathVariables() {
        return 3;
    }

    @Override
    public Integer getMaxQueryParameters() {
        return 3;
    }

    @Override
    public Integer getMaxQueryValues() {
        return 3;
    }
}

class TierInfinity implements Subscription {

    private static TierInfinity singleton;

    public static TierInfinity getInstance() {
        if (singleton == null) {
            singleton = new TierInfinity();
        }
        return singleton;
    }

    private TierInfinity() {};


    @Override
    public String getTier() {
        return "TIER_INFINITY";
    }

    // all methods below return "3" just to ease testing !!

    @Override
    public Integer getMaxNumLevels() {
        return null;
    }

    @Override
    public Integer getMaxLevelNames() {
        return null;
    }

    @Override
    public Integer getMaxPathVariables() {
        return null;
    }

    @Override
    public Integer getMaxQueryParameters() {
        return null;
    }

    @Override
    public Integer getMaxQueryValues() {
        return null;
    }
}
