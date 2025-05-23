package org.company.entities;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import java.util.ArrayList;
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
    
    - DataEncoded: List[level1Data, level2Data, ... ] where each levelData is a hash map that saves the encoded data seen in the given level. For example: {"some_value": "some_hash"}
    
    */

    // is it necessary to define an @Id field ? not necessarily, it can be generated by MongoDb... so probably for the best.

    @Id
    private String id;

    @DocumentReference
    private Company company;

    private String companyDomainHashed;

    private List<Map<String, String>> dataEncoded;
    private List<Map<String, String>> dataDecoded;

    public CompanyUrlData(String id, Company company, String companyDomainHashed) {
        this.id = id;
        this.company = company;
        this.companyDomainHashed = companyDomainHashed;
        this.dataEncoded = new ArrayList<>();
        this.dataDecoded = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getCompanyDomainHashed() {
        return companyDomainHashed;
    }


    // private getters for Jackson
    public List<Map<String, String>> getDataEncoded() {
        return dataEncoded;
    }

    public List<Map<String, String>> getDataDecoded() {
        return dataDecoded;
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
    private void setCompanyDomainHashed(String companyDomainHashed) {
        this.companyDomainHashed = companyDomainHashed;
    }

    @SuppressWarnings("unused")
    private void setDataEncoded(List<Map<String, String>> dataEncoded) {
        this.dataEncoded = dataEncoded;
    }

    @SuppressWarnings("unused")
    private void setDataDecoded(List<Map<String, String>> dataDecoded) {
        this.dataDecoded = dataDecoded;
    }

    @SuppressWarnings("unused")
    private void setId(String id) {
        this.id = id;
    }
}
