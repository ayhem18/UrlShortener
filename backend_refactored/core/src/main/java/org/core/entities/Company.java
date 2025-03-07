package org.core.entities;

import com.fasterxml.jackson.annotation.*;
import org.access.Subscription;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Company")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation Making Jackson ignore all null fields
public class Company {
    public static final String COMPANY_COLLECTION_NAME = "COMPANY";

    // all ids should be read-only
    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id; // some sort of Company identifier

    private String topLevelDomain;

    private String domainHash;

    private String emailDomain;

    // written but never read...
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Subscription subscription;

    // the constructor is only meant to be called through a CompanyWrapper object
    // this is enforced by the default visibility value
    // the constructor can be called only from other files within the same package as Company.java

    public Company(String id,
                String topLevelDomain,
                String domainHash,
                Subscription subscription,
                String emailDomain) {

            this.id = id;
            this.topLevelDomain = topLevelDomain;
            this.domainHash = domainHash;
            this.subscription = subscription;
            this.emailDomain = emailDomain;
    }

    // create a constructor that would set the emailDomain to null
    public Company(String id, String topLevelDomain, String domainHash, Subscription subscription) {
        this(id, topLevelDomain, domainHash, subscription, null);
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
        if (serializeSensitiveCount < 2) {
            this.serializeSensitiveCount += 1;
            return this.id;
        }
        return null;
    }

    @JsonGetter(value = "domainHash")
    private String jsonGetDomainHash() {
        if (this.serializeSensitiveCount < 2) {
            this.serializeSensitiveCount += 1;
            return this.domainHash;
        }
        return null;
    }


    private String getId() {
        return id;
    }

    private int getSerializeSensitiveCount() {
        return serializeSensitiveCount;
    }


    @JsonGetter(value = "subscription")
    String getSub() {
        return subscription.getTier();
    }

    @JsonGetter(value = "domain")
    String getDomain() {
        return topLevelDomain;
    }

    Subscription getSubscription() {
        return subscription;
    }

    String getDomainHash() {
        return this.domainHash;
    }


    ///////////////////////////////// SETTERS /////////////////////////////////////////////
    void setDomain(String domain) {
        this.topLevelDomain = domain;
    }

    void setId(String id) {
        this.id = id;
    }

    // the subscription can be changed
    void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // add private setters for Jackson serialization (private so they can't be set by the program)
    private void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }

    private void setDomainHash(String domainHash) {
        this.domainHash = domainHash;
    }

    ///////////////////////////////// OTHER /////////////////////////////////////////////

    @Override
    public String toString() {
        return "Company{" +
                "domain='" + topLevelDomain + '\'' +
                '}';
    }

}



