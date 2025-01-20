package org.data.entities;

import org.example.UrlEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.utils.CustomGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this class represents the dataEncoded unit that saves all the information related to company urls

@Document
public class CompanyUrlData {

    private Company company;

    private String companySiteHash;

    private List<HashMap<UrlEntity, HashMap<String, String>>> dataEncoded;
    private List<HashMap<UrlEntity, HashMap<String, String>>> dataDecoded;

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

    public List<HashMap<UrlEntity, HashMap<String, String>>> getDataEncoded() {
        return dataEncoded;
    }

    // if Jackson uses reflection to access the fields, then making the setters private shouldn't be an issue
    private void setDataEncoded(List<HashMap<UrlEntity, HashMap<String, String>>> dataEncoded) {
        this.dataEncoded = dataEncoded;
    }

    public List<HashMap<UrlEntity, HashMap<String, String>>> getDataDecoded() {
        return dataDecoded;
    }

    private void setDataDecoded(List<HashMap<UrlEntity, HashMap<String, String>>> dataDecoded) {
        this.dataDecoded = dataDecoded;
    }

    private HashMap<UrlEntity, HashMap<String, String>> initializeLevel() {
        HashMap<UrlEntity, HashMap<String, String>> values = new HashMap<>();
        values.put(UrlEntity.LEVEL_NAME, new HashMap<>());
        values.put(UrlEntity.PATH_VARIABLE, new HashMap<>());
        values.put(UrlEntity.QUERY_PARAM, new HashMap<>());
        values.put(UrlEntity.QUERY_PARAM_VALUE, new HashMap<>());
        return values;
    }

    public Map.Entry<HashMap<String, String>, HashMap<String, String>> getLevelTypeData(int level, UrlEntity valueType) {
        // the level is expected to be 1-indexed
        level -= 1;

        if (this.dataEncoded.size() == level) {
            HashMap<UrlEntity, HashMap<String, String>> levelValues = this.initializeLevel();
            HashMap<UrlEntity, HashMap<String, String>> levelValuesDecoded = this.initializeLevel();
            this.dataEncoded.add(levelValues);
            this.dataDecoded.add(levelValuesDecoded);
        }

        if (this.dataEncoded.size() + 1 <= level) {
            throw new RuntimeException("Make sure the levels are added consecutively");
        }

        // I use the Map.Entry class here since there is no Pair class in Java
        return Map.entry(this.dataEncoded.get(level).get(valueType),
                this.dataDecoded.get(level).get(valueType));
    }

    public int count(int level, UrlEntity valueType) {
        return getLevelTypeData(level, valueType).getKey().size();
    }

    public void addValue(int level, UrlEntity valueType, String value, CustomGenerator gen) {
        Map.Entry<HashMap<String, String>, HashMap<String, String>>
                levelData =  getLevelTypeData(level, valueType);

        HashMap<String, String> levelDataEncoded = levelData.getKey();

        // only update the hashmap if the value has not been already saved in the database
        if (levelDataEncoded.containsKey(value)) {
            return;
        }

        HashMap<String, String> levelDataDecoded= levelData.getValue();

        int itemsCount = levelData.getKey().size();
        String valueHash = gen.generateId(itemsCount);


        levelDataEncoded.put(value, valueHash);
        levelDataDecoded.put(valueHash, value);
    }

}
