package org.company.entities;

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
    private String id; // some sort of Company identifier

    private String emailDomain;

    private String ownerEmail; 

    // write only 
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Subscription subscription;

    private boolean verified;

    // Updated constructor to include ownerEmail
    public Company(String id,
                Subscription subscription,
                String emailDomain,
                String ownerEmail) {
            this.id = id;
            this.subscription = subscription;
            this.emailDomain = emailDomain;
            this.ownerEmail = ownerEmail;
            this.verified = false;
    }


    // a private constructor for Jackson serialization
    @SuppressWarnings("unused")
    private Company() {
    }

    ///////////////////////////////// JSON GETTERS /////////////////////////////////////////////

    // one trick to serialize fields conditionally is to write a JsonGetter method
    // that checks the condition on the fly, returning Null when the condition is not verified
    @JsonGetter(value = "id")
    private String jsonGetId() {
        // 2 represents the number of sensitive fields that should be serialized only once:
        // when saved into the database
        if (serializeSensitiveCount < 2) {
            this.serializeSensitiveCount += 1;
            return this.id;
        }
        return null;
    }

    @JsonGetter(value = "ownerEmail")
    private String jsonGetEmailOwner() {
        // 2 represents the number of sensitive fields that should be serialized only once:
        // when saved into the database
        if (serializeSensitiveCount < 2) {
            this.serializeSensitiveCount += 1;
            return this.ownerEmail;
        }
        return null;
    }

    // @JsonGetter(value = "subscription")
    // public String getSub() {
    //     return subscription.getTier();
    // }


    @JsonGetter(value = "emailDomain")
    public String getEmailDomain() {
        return emailDomain;
    }

    public String getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public boolean getVerified() {
        return verified;
    }

    @SuppressWarnings("unused")
    private int getSerializeSensitiveCount() {
        return serializeSensitiveCount;
    }

    ///////////////////////////////// SETTERS /////////////////////////////////////////////
    void setId(String id) {
        this.id = id;
    }

    // the subscription can be changed
    void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    // Add setter for ownerEmail
    void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    // add private setters for Jackson serialization (private so they can't be set by the program)
    @SuppressWarnings("unused")
    private void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }
    
    // Add Jackson-specific setter with SuppressWarnings
    @SuppressWarnings("unused")
    private void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public void verify() {
        if (this.verified) {
            throw new IllegalStateException("The company is already verified");
        }
        this.verified = true;
    }
}



