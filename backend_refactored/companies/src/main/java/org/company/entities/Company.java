package org.company.entities;

import com.fasterxml.jackson.annotation.*;
import org.access.Subscription;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import io.swagger.v3.oas.annotations.media.Schema;

@SuppressWarnings("unused")
@Document("Company")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation Making Jackson ignore all null fields
@Schema(description = "Company entity representing a business organization")
public class Company {
    public static final String COMPANY_COLLECTION_NAME = "COMPANY";

    @Schema(description = "Unique identifier for the company", example = "company_12345")
    @Id
    private String id; // must be unique

    // the name of the company
    @Schema(description = "Name of the company", example = "Example Corp")
    private String companyName; // must be unique

    @Schema(description = "Physical address of the company", example = "123 Business St, Suite 100")
    private String companyAddress; // not sure worth adding the unique constraint here 

    @Schema(description = "Domain for company emails", example = "example.com")
    private String emailDomain; // determines the email domain enforced by the company (if any)

    @Schema(description = "Email of the company owner", example = "owner@example.com")
    private String ownerEmail; // the email of the owner of the company

    // write only: a field used to track the number of times the sensitive fields are serialized 
    @Schema(description = "Counter for sensitive field serialization", hidden = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    @Schema(description = "Company's subscription level", implementation = Subscription.class)
    private Subscription subscription;

    @Schema(description = "Verification status of the company", example = "true")
    private boolean verified;

    // Updated constructor to include ownerEmail
    public Company(String id, String companyName, String companyAddress, String ownerEmail, String emailDomain, Subscription subscription) {
        
        // make sure the email domain matches the owner email
        if (emailDomain != null && !ownerEmail.endsWith(emailDomain)) {
            throw new IllegalArgumentException("The email domain does not match the company domain");
        }

        this.id = id;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.ownerEmail = ownerEmail;
        this.emailDomain = emailDomain;
        this.subscription = subscription;
        this.verified = false;
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

    // can verify the company (well at most once)
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

    ///////////////////////////////// OTHER /////////////////////////////////////////////
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } 
        
        if (other instanceof Company otherCompany) {
            return this.id.equals(otherCompany.getId());
        }
        return false;
    }
}



