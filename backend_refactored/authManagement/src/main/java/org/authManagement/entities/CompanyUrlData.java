package org.authManagement.entities;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.company.entities.Company;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.url.UrlEntity;
import org.utils.CustomGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Document
@JsonPropertyOrder({"company", "companySiteHash", "dataEncoded", "dataDecoded"})
public class CompanyUrlData {
    /*
    This class is meant to save the Url data for a given Company. The data is used to encode and decode urls
    used by said company (which means the top-level of each url is assumed to be Company site)

    Fields:

    - Company: company object
    - companySiteHash: an encoding of the company: saved independently as it will be used for each shorter url
    - dataEncoded: natural strings -> hashes
    - dataDecoded: hashes -> natural string , this way both operations are optimized (for the cost of double memory usage)

    - DataEncoded: List[item1, item2, ... ] where each item represents a hash map
    item_i : {valueType: {map_i}}
    map_i: represents {string of type: ValueType -> hash}
    * */

    @DocumentReference
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

    public Company getCompany() {
        return company;
    }

    public String getCompanySiteHash() {
        return companySiteHash;
    }

    public List<HashMap<UrlEntity, HashMap<String, String>>> getDataEncoded() {
        return dataEncoded;
    }

    public List<HashMap<UrlEntity, HashMap<String, String>>> getDataDecoded() {
        return dataDecoded;
    }

    private HashMap<UrlEntity, HashMap<String, String>> initializeLevel() {
        HashMap<UrlEntity, HashMap<String, String>> values = new HashMap<>();
        values.put(UrlEntity.LEVEL_NAME, new HashMap<>());
        values.put(UrlEntity.PATH_VARIABLE, new HashMap<>());
        values.put(UrlEntity.QUERY_PARAM, new HashMap<>());
        values.put(UrlEntity.QUERY_PARAM_VALUE, new HashMap<>());
        return values;
    }

    public Map.Entry<HashMap<String, String>, HashMap<String, String>>
    getLevelTypeData(int level, UrlEntity valueType) {
        if (level <= 0) {
            throw new IllegalArgumentException("The level argument is expected to be 1-indexed !!!");
        }

        // the level is expected to be 1-indexed
        level -= 1;

        if (this.dataEncoded.size() == level) {
            HashMap<UrlEntity, HashMap<String, String>> levelValues = this.initializeLevel();
            HashMap<UrlEntity, HashMap<String, String>> levelValuesDecoded = this.initializeLevel();
            this.dataEncoded.add(levelValues);
            this.dataDecoded.add(levelValuesDecoded);
        }

        if (this.dataEncoded.size() + 1 <= level) {
            throw new IllegalArgumentException("Make sure the levels are added consecutively. " +
                    "The current maximum level is " + this.dataEncoded.size());
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

        // only update the hashmap if the value has not been already saved
        if (levelDataEncoded.containsKey(value)) {
            return;
        }

        HashMap<String, String> levelDataDecoded= levelData.getValue();

        int itemsCount = levelData.getKey().size();
        String valueHash = gen.generateId(itemsCount);

        levelDataEncoded.put(value, valueHash);
        levelDataDecoded.put(valueHash, value);
    }

    // private setters and no-arg constructor added so that Jackson can work properly
    @SuppressWarnings("unused")
    private CompanyUrlData() {
        this.dataEncoded = new ArrayList<>();
        this.dataDecoded = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }

    @SuppressWarnings("unused")
    private void setCompanySiteHash(String companySiteHash) {
        this.companySiteHash = companySiteHash;
    }

    @SuppressWarnings("unused")
    private void setDataEncoded(List<HashMap<UrlEntity, HashMap<String, String>>> dataEncoded) {
        this.dataEncoded = dataEncoded;
    }

    @SuppressWarnings("unused")
    private void setDataDecoded(List<HashMap<UrlEntity, HashMap<String, String>>> dataDecoded) {
        this.dataDecoded = dataDecoded;
    }
}
