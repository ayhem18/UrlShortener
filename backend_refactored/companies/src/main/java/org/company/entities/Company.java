package org.company.entities;

import com.fasterxml.jackson.annotation.*;
import org.access.Subscription;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Company")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation Making Jackson ignore all null fields
public class Company {
    public static final String COMPANY_COLLECTION_NAME = "COMPANY";

    @Id
    private String id; // must be unique

    // the name of the company
    private String companyName; // must be unique

    private String companyAddress; // not sure worth adding the unique constraint here 

    private String emailDomain; // determines the email domain enforced by the company (if any)

    private String ownerEmail; // the email of the owner of the company

    // write only: a field used to track the number of times the sensitive fields are serialized 
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Subscription subscription;

    private boolean verified;

    // Updated constructor to include ownerEmail
    public Company(String id, String companyName, String companyAddress, String ownerEmail, String emailDomain, Subscription subscription) {
        
        // make sure the email domain matches the owner email
        if (emailDomain != null && !ownerEmail.endsWith(emailDomain)) {
            throw new IllegalArgumentException("The email domain does not match the company domain");
        }

        this.id = id;
        this.subscription = subscription;
        this.ownerEmail = ownerEmail;
        this.emailDomain = emailDomain;
        this.verified = false;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    // a private no-args constructor for Jackson serialization
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


    @JsonGetter(value = "companyName")
    public String getCompanyName() {
        return companyName;
    }

    @JsonGetter(value = "companyAddress")
    public String getCompanyAddress() {
        return companyAddress;
    }

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

    
    ///////////////////////////////// actual setters /////////////////////////////////////////////
    // can change the company name
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // can change the company address
    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    // can change the subscription
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // can verify the company (well atmost once)
    public void verify() {
        if (this.verified) {
            throw new IllegalStateException("The company is already verified");
        }
        this.verified = true;
    }

    ///////////////////////////////// JACKSON PRIVATE SETTERS /////////////////////////////////////////////
    @SuppressWarnings("unused") // cannot set the id
    private void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused") // setting the ownerEmail might be very problematic
    private void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    @SuppressWarnings("unused")
    private void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }


    @SuppressWarnings("unused") // setting the emailDomain might be very problematic
    private void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }
}



