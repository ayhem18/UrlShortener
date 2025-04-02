package org.access;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Subscription {

    // @JsonValue
    String getTier();

    Integer getMaxNumLevels();

    // constraints on the number of admins and employees    
    Integer getMaxAdmins();

    Integer getMaxEmployees();

    default Integer getMaxUsers(Role role) {
        if (role == RoleManager.getRole(RoleManager.ADMIN_ROLE)) {
            return this.getMaxAdmins();
        } else if (role == RoleManager.getRole(RoleManager.EMPLOYEE_ROLE)) {
            return this.getMaxEmployees();
        } else {
            // the line below will throw an appropriate error
            RoleManager.getRole("");
            // the return statement below is for the compiler
            return 0;
        }
    }

    @SuppressWarnings("unused") // this function is used whenever a Subscription object is serialized
    @JsonValue
    default String getJsonValue() throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("tier", this.getTier());
        map.put("maxNumLevels", this.getMaxNumLevels() == null ? "no limit" : this.getMaxNumLevels().toString());
        map.put("maxAdmins", this.getMaxAdmins().toString());
        map.put("maxEmployees", this.getMaxEmployees().toString());
        map.put("maxHistorySize", this.getMaxHistorySize().toString());
        map.put("encodingDailyLimit", this.getEncodingDailyLimit() == null ? "no limit" : this.getEncodingDailyLimit().toString());
        map.put("minUrlLength", this.getMinUrlLength().toString());
        map.put("minParameterLength", this.getMinParameterLength().toString());
        map.put("minVariableLength", this.getMinVariableLength().toString());
        return new ObjectMapper().writeValueAsString(map);
    }


    // constraints on the size of the history
    Integer getMaxHistorySize();

    Integer getEncodingDailyLimit();

    Integer getMinUrlLength();

    Integer getMinParameterLength();

    Integer getMinVariableLength();


}


class FreeTier implements Subscription {
    private static FreeTier singleton;

    public static FreeTier getInstance() {
        if (singleton == null) {
            singleton = new FreeTier();
        }
        return singleton;
    }

    private FreeTier() {}

    @Override
    public String getTier() {
        return "FREE";
    }

    // constraints on url encoding

    @Override
    public Integer getMaxNumLevels() {
        return 5;
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

    @Override
    public Integer getEncodingDailyLimit() {
        return 20;
    }

    @Override
    public Integer getMinUrlLength() {
        return 40;
    }

    @Override
    public Integer getMinParameterLength() {
        return 15;
    }

    @Override
    public Integer getMinVariableLength() {
        return 20;
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

    private TierOne() {}


    @Override
    public String getTier() {
        return "TIER_1";
    }

    @Override
    public Integer getMaxNumLevels() {
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

    @Override
    public Integer getEncodingDailyLimit() {
        return 100;
    }

    @Override
    public Integer getMinUrlLength() {
        return 25;
    }

    @Override
    public Integer getMinParameterLength() {
        return 15;
    }

    @Override
    public Integer getMinVariableLength() {
        return 15;
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

    private TierInfinity() {}


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

    @Override
    public Integer getEncodingDailyLimit() {
        return null;
    }

    @Override
    public Integer getMinUrlLength() {
        return 0;
    }

    @Override
    public Integer getMinParameterLength() {
        return 5;
    }

    @Override
    public Integer getMinVariableLength() {
        return 5;
    }
}
