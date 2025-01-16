package com.url_shortener.Urls.UrlData;

import com.url_shortener.CustomGenerator;
import com.url_shortener.Service.Company.Company;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// this class represents the data unit that saves all the information related to company urls

@Document
public class CompanyUrlData {
    private static final String LEVEL_NAME = "levelName";
    private static final String PATH_VARIABLE = "pathVariable";
    private static final String QUERY_PARAM = "queryParameter";
    private static final String QUERY_PARAM_VALUE = "queryParameterValue";
    private static final List<String> VALUE_TYPES  = List.of(LEVEL_NAME.toLowerCase(),
            PATH_VARIABLE.toLowerCase(),
            QUERY_PARAM.toLowerCase(),
            QUERY_PARAM_VALUE.toLowerCase());

    private Company company;

    private String companySiteHash;

    private List<HashMap<String, HashMap<String, String>>> data;


    public CompanyUrlData(Company company, String companySiteHash) {
        this.company = company;
        this.companySiteHash = companySiteHash;
        this.data = new ArrayList<>();
    }

    public CompanyUrlData() {
        this.data = new ArrayList<>();
    }

    public Company getCompany() {
        return company;
    }

    private void setCompany(Company company) {
        this.company = company;
    }

    public String getCompanySiteHash() {
        return companySiteHash;
    }

    private void setCompanySiteHash(String companySiteHash) {
        this.companySiteHash = companySiteHash;
    }

    public List<HashMap<String, HashMap<String, String>>> getData() {
        return data;
    }

    // if Jackson uses reflection to access the fields, then making the setter private shouldn't be an issue
    private void setData(List<HashMap<String, HashMap<String, String>>> data) {
        this.data = data;
    }


    private HashMap<String, HashMap<String, String>> initializeLevel() {
        HashMap<String, HashMap<String, String>> values = new HashMap<>();
        values.put(LEVEL_NAME, new HashMap<>());
        values.put(PATH_VARIABLE, new HashMap<>());
        values.put(QUERY_PARAM, new HashMap<>());
        values.put(QUERY_PARAM_VALUE, new HashMap<>());
        return values;
    }


    public void addValue(int level, String valueType, String value, CustomGenerator gen) {
        if (! VALUE_TYPES.contains(valueType.toLowerCase())) {
            throw new RuntimeException("Undefined value type");
        }

        if (this.data.size() < level) {
            HashMap<String, HashMap<String, String>> levelValues = this.initializeLevel();
            this.data.add(levelValues);
        }

        HashMap<String, String> levelData = this.data.get(level).get(valueType.toLowerCase());

        int itemsCount = levelData.size() / 2;

        String valueHash = gen.generateId(itemsCount);

        // add the pairs <value, valueHash>, <valueHash, value>
        levelData.put(value, valueHash);
        levelData.put(valueHash, value);
    }

}
