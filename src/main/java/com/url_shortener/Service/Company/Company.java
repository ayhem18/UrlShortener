package com.url_shortener.Service.Company;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Document("Company") // make sure to use the Document annotation and not the @Entity since this is not a SQL table...

public class Company {
    private static long COMPANY_COUNT = 0;

    private static String generateSiteId() {
        // use the COMPANY_COUNT static field
        long number = COMPANY_COUNT;

        StringBuilder instanceId = new StringBuilder();

        while (number >= 26) {
            instanceId.append("z");
            number = number / 26;
        }

        int asciiOfA = 'a';
        instanceId.append((char)(asciiOfA + number));
        return instanceId.toString();
    }

    // all ids should be read-only
    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id; // some sort of Company identifier

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String siteId;

    private String site;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> roleTokens;

    private int serializeSensitiveCount;

    private Map<String, String> roleTokensHashed;

    public Company(String id, String site, Map<String, String> roleTokens, PasswordEncoder encoder) {
        this.id = id;
        // generate id uses the 25-based site id
        this.siteId = generateSiteId();
        // make sure to increment the count
        COMPANY_COUNT += 1;
        this.site = site;

        this.setTokens(roleTokens, encoder);
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
    // not sure if the "value" field in the annotation refers to the field in the class
    // or the name displayed in the resulting Json
    @JsonGetter(value = "id")
    private String jsonGetId() {
        // 4 represents the number of sensitive fields that should be serialized only once:
        // when saved into the database
        if (serializeSensitiveCount < 4) {
            serializeSensitiveCount += 1;
            return this.id;
        }
        return null;
    }

    @JsonGetter(value = "siteId")
    private String jsonGetSiteId() {
        if (serializeSensitiveCount < 4) {
            serializeSensitiveCount += 1;
            return this.siteId;
        }
        return null;
    }

    @JsonGetter(value = "roleTokens")
    private Map<String, String> jsonGetRoleTokens() {
        if (serializeSensitiveCount < 4) {
            serializeSensitiveCount += 1;
            return this.roleTokens;
        }
        return null;
    }

    @JsonGetter(value = "roleHashedTokens")
    private Map<String, String> jsonGetRoleTokensHashed() {
        if (serializeSensitiveCount < 4) {
            serializeSensitiveCount += 1;
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
}




