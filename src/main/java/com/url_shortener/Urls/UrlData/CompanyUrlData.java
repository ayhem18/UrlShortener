package com.url_shortener.Urls.UrlData;

import com.url_shortener.CustomGenerator;
import com.url_shortener.Service.Company.Company;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this class represents the dataEncoded unit that saves all the information related to company urls

@Document
public class CompanyUrlData {
    public static final String LEVEL_NAME = "levelName";
    public static final String PATH_VARIABLE = "pathVariable";
    public static final String QUERY_PARAM = "queryParameter";
    public static final String QUERY_PARAM_VALUE = "queryParameterValue";
    public static final List<String> VALUE_TYPES  = List.of(LEVEL_NAME.toLowerCase(),
            PATH_VARIABLE.toLowerCase(),
            QUERY_PARAM.toLowerCase(),
            QUERY_PARAM_VALUE.toLowerCase()
    );

    private Company company;

    private String companySiteHash;

    private List<HashMap<String, HashMap<String, String>>> dataEncoded;
    private List<HashMap<String, HashMap<String, String>>> dataDecoded;

    public CompanyUrlData(Company company, String companySiteHash) {
        this.company = company;
        this.companySiteHash = companySiteHash;
        this.dataEncoded = new ArrayList<>();
        this.dataDecoded = new ArrayList<>();
    }

    public CompanyUrlData() {
        this.dataEncoded = new ArrayList<>();
        this.dataDecoded = new ArrayList<>();
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

    public List<HashMap<String, HashMap<String, String>>> getDataEncoded() {
        return dataEncoded;
    }

    // if Jackson uses reflection to access the fields, then making the setters private shouldn't be an issue
    private void setDataEncoded(List<HashMap<String, HashMap<String, String>>> dataEncoded) {
        this.dataEncoded = dataEncoded;
    }

    public List<HashMap<String, HashMap<String, String>>> getDataDecoded() {
        return dataDecoded;
    }

    private void setDataDecoded(List<HashMap<String, HashMap<String, String>>> dataDecoded) {
        this.dataDecoded = dataDecoded;
    }

    private HashMap<String, HashMap<String, String>> initializeLevel() {
        HashMap<String, HashMap<String, String>> values = new HashMap<>();
        values.put(LEVEL_NAME, new HashMap<>());
        values.put(PATH_VARIABLE, new HashMap<>());
        values.put(QUERY_PARAM, new HashMap<>());
        values.put(QUERY_PARAM_VALUE, new HashMap<>());
        return values;
    }

    public Map.Entry<HashMap<String, String>, HashMap<String, String>> getLevelTypeData(int level, String valueType) {
        if (! VALUE_TYPES.contains(valueType.toLowerCase())) {
            throw new RuntimeException("Undefined value type");
        }

        level -= 1;

        if (this.dataEncoded.size() == level) {
            HashMap<String, HashMap<String, String>> levelValues = this.initializeLevel();
            HashMap<String, HashMap<String, String>> levelValuesDecoded = this.initializeLevel();
            this.dataEncoded.add(levelValues);
            this.dataDecoded.add(levelValuesDecoded);
        }

        if (this.dataEncoded.size() + 1 <= level) {
            throw new RuntimeException("Make sure the levels are added consecutively");
        }

        // I use the Map.Entry class here since there is no Pair class in Java
        return Map.entry(this.dataEncoded.get(level).get(valueType.toLowerCase()),
                this.dataDecoded.get(level).get(valueType.toLowerCase()));
    }

    public int count(int level, String valueType) {
        return getLevelTypeData(level, valueType).getKey().size();
    }

    public void addValue(int level, String valueType, String value, CustomGenerator gen) {
        Map.Entry<HashMap<String, String>, HashMap<String, String>>
                levelData =  getLevelTypeData(level, valueType);

        int itemsCount = levelData.getKey().size();
        String valueHash = gen.generateId(itemsCount);

        HashMap<String, String> levelDataEncoded = levelData.getKey();
        HashMap<String, String> levelDataDecoded= levelData.getValue();

        //
        levelDataEncoded.put(value, valueHash);
        levelDataDecoded.put(valueHash, value);
    }

}
