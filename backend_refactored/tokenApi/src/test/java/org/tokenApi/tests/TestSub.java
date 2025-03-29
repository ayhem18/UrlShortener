package org.tokenApi.tests;


import org.access.Subscription;

class SubTest1 implements Subscription {
    @Override
    public String getTier() {
        return "test";
    }

    @Override
    public Integer getMaxNumLevels() {
        return 5;
    }

    @Override
    public Integer getMaxAdmins() {
        return 10;
    }

    @Override
    public Integer getMaxEmployees() {
        return 10;
    }

    @Override
    public Integer getMaxHistorySize() {
        return 10;
    }

    @Override
    public Integer getEncodingDailyLimit() {
        return 10;
    }

    @Override
    public Integer getMinUrlLength() {
        return 20;
    }

    @Override
    public Integer getMinParameterLength() {
        return 4;
    }

    @Override
    public Integer getMinVariableLength() {
        return 4;
    }
}

class SubTest2 implements Subscription {
    @Override
    public String getTier() {
        return "test";
    }

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
        return 1;
    }

    @Override
    public Integer getMaxHistorySize() {
        return 1;
    }

    @Override
    public Integer getEncodingDailyLimit() {
        return 1;
    }

    @Override
    public Integer getMinUrlLength() {
        return 100;
    }

    @Override
    public Integer getMinParameterLength() {
        return 8;
    }

    @Override
    public Integer getMinVariableLength() {
        return 8;
    }
}



