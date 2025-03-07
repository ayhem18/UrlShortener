package org.access;


import org.url.UrlEntity;

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

    // constraints on the number of admins and employees    
    Integer getMaxAdmins();

    Integer getMaxEmployees();

    // constraints on the size of the history
    Integer getMaxHistorySize();

}


class Free implements Subscription {
    private static Free singleton;

    public static Free getInstance() {
        if (singleton == null) {
            singleton = new Free();
        }
        return singleton;
    }

    private Free() {};

    @Override
    public String getTier() {
        return "FREE";
    }

    // constraints on url encoding

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

    @Override
    public Integer getMaxAdmins() {
        return 1;
    }

    @Override
    public Integer getMaxEmployees() {
        return 2;
    }
    
    @Override
    public Integer getMaxHistorySize() {
        return 0;
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

    @Override
    public Integer getMaxNumLevels() {
        return 10;
    }

    @Override
    public Integer getMaxLevelNames() {
        return 10;
    }

    @Override
    public Integer getMaxPathVariables() {
        return 10;
    }

    @Override
    public Integer getMaxQueryParameters() {
        return 10;
    }

    @Override
    public Integer getMaxQueryValues() {
        return 10;
    }

    @Override
    public Integer getMaxAdmins() {
        return 2;
    }

    @Override
    public Integer getMaxEmployees() {
        return 6;
    }

    @Override
    public Integer getMaxHistorySize() {
        return 10;
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

    @Override
    public Integer getMaxAdmins() {
        return 3;
    }

    @Override
    public Integer getMaxEmployees() {
        return 10;
    }

    @Override
    public Integer getMaxHistorySize() {
        return 100;
    }
}
