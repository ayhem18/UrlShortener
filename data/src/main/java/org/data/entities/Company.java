package org.data.entities;

import com.fasterxml.jackson.annotation.*;
import org.common.Subscription;
import org.springframework.data.annotation.Id;

import java.util.Map;

// make sure to use the Document annotation and not the @Entity since this is not a SQL table...
//@Document("Company")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation Making Jackson ignore all null fields
public class Company {
    public static final String COMPANY_COLLECTION_NAME = "COMPANY";

    // all ids should be read-only
    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id; // some sort of Company identifier

    private String siteHash;

    private String site;

    private Map<String, String> roleTokens;

    private Map<String, String> roleTokensHashed;

    // written but never read...
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Subscription subscription;

    // the constructor is only meant to be called through a CompanyWrapper object
    // this is enforced by the default visibility value
    // the constructor can be called only from other files within the same package as Company.java
    Company(String id,
            String site,
            String siteHash,
            Map<String, String> roleTokens,
            Map<String, String> roleTokensHashed,
            Subscription subscription) {
        this.id = id;
        this.siteHash = siteHash;
        this.site = site;
        this.roleTokens = roleTokens;
        this.roleTokensHashed = roleTokensHashed;
        this.subscription = subscription;
    }


    // a private constructor for Jackson serialization
    private Company() {
    }

    ///////////////////////////////// JSON GETTERS /////////////////////////////////////////////

    // one trick to serialize fields conditionally is to write a JsonGetter method
    // that checks the condition on the fly, returning Null when the condition is not verified
    @JsonGetter(value = "id")
    private String jsonGetId() {
        // 4 represents the number of sensitive fields that should be serialized only once:
        // when saved into the database
        if (serializeSensitiveCount < 4) {
            this.serializeSensitiveCount += 1;
            return this.id;
        }
        return null;
    }

    @JsonGetter(value = "siteHash")
    private String jsonGetSiteId() {
        if (this.serializeSensitiveCount < 4) {
            this.serializeSensitiveCount += 1;
            return this.siteHash;
        }
        return null;
    }

    @JsonGetter(value = "roleTokens")
    private Map<String, String> jsonGetRoleTokens() {
        if (this.serializeSensitiveCount < 4) {
            this.serializeSensitiveCount += 1;
            return this.roleTokens;
        }
        return null;
    }

    @JsonGetter(value = "roleTokensHashed")
    private Map<String, String> jsonGetRoleTokensHashed() {
        if (this.serializeSensitiveCount < 4) {
            this.serializeSensitiveCount += 1;
            return this.roleTokensHashed;
        }
        return null;
    }


    ///////////////////////////////// Standard GETTERS /////////////////////////////////////////////
    @JsonIgnore
    Map<String, String> getRoleTokens() {
        return roleTokens;
    }

    Map<String, String> getRoleTokensHashed() {
        return roleTokensHashed;
    }

    String getId() {
        return id;
    }

    private int getSerializeSensitiveCount() {
        return serializeSensitiveCount;
    }


    @JsonGetter(value = "subscription")
    String getSub() {
        return subscription.getTier();
    }

    @JsonGetter(value = "site")
    String getSite() {
        return site;
    }

    Subscription getSubscription() {
        return subscription;
    }

    String getSiteHash() {
        return this.siteHash;
    }


    ///////////////////////////////// SETTERS /////////////////////////////////////////////
    void setRoleTokens(Map<String, String> roleTokens) {
        this.roleTokens = roleTokens;
    }

    void setRoleTokensHashed(Map<String, String> roleTokensHashed) {
        this.roleTokensHashed = roleTokensHashed;
    }

    void setSite(String site) {
        this.site = site;
    }

    void setId(String id) {
        this.id = id;
    }

    // the subscription can be changed
    void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // add private setters for Jackson serialization (private so they can't be set by the program)
    void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }

    void setSiteHash(String siteHash) {
        this.siteHash = siteHash;
    }

    ///////////////////////////////// OTHER /////////////////////////////////////////////

    @Override
    public String toString() {
        return "Company{" +
                "site='" + site + '\'' +
                '}';
    }

}




