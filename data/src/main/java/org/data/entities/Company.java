package org.data.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.common.Subscription;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

import java.util.HashMap;
import java.util.Map;

// make sure to use the Document annotation and not the @Entity since this is not a SQL table...
@Document("Company")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation ignoring all null fields
public class Company {
    private static long COMPANY_COUNT = 0;

    // all ids should be read-only
    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id; // some sort of Company identifier

    private String siteId;

    private String site;

    private Map<String, String> roleTokens;

    private Map<String, String> roleTokensHashed;

    // written but never read...
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Subscription subscription;

    public Company(String id, String site, Subscription sub,
                   Map<String, String> roleTokens, PasswordEncoder encoder, CustomGenerator gen) {
        this.id = id;
        // generate id uses the 25-based site id
        this.siteId = gen.generateId(COMPANY_COUNT);
        // make sure to increment the count
        COMPANY_COUNT += 1;

        this.site = site;

        this.setTokens(roleTokens, encoder);

        this.subscription = sub;

        this.serializeSensitiveCount = 0;
    }

    public Company() {
    }

    @Override
    public String toString() {
        return "Company{" +
                "site='" + site + '\'' +
                '}';
    }
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

    @JsonGetter(value = "siteId")
    private String jsonGetSiteId() {
        if (this.serializeSensitiveCount < 4) {
            this.serializeSensitiveCount += 1;
            return this.siteId;
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    // changed the name of the getter from the standard java convention so that the Mongodb driver wouldn't use it
    // added the JsonIgnore annotation so that Json would not create a field "tokens" when serializing a Company Object
    @JsonIgnore
    public Map<String, String> getTokens() {
        return this.roleTokensHashed;
    }

    public void setTokens(Map<String, String> roleTokens, PasswordEncoder encoder) {
        // the method signature ensures that the hashes are always persistent with the actual tokens
        // set the field
        this.roleTokens = roleTokens;

        // deep Copy the role Tokens
        this.roleTokensHashed = new HashMap<>(this.roleTokens);

        // encoder the tokens
        for (Map.Entry<String, String> entry : this.roleTokensHashed.entrySet()) {
            entry.setValue(encoder.encode(entry.getValue())); // make sure to encode the value !! and not the key !!!
        }
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // added mainly for correct Json serialization
    private int getSerializeSensitiveCount() {
        return serializeSensitiveCount;
    }

    public void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }
}




