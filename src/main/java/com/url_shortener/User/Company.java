package com.url_shortener.User;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

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
    String id; // some sort of Company identifier

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String siteId;


//    @Column(nullable = false, unique = true) // the site must be unique as well
    // the flexibility of MongoDB comes with the complete lack of any constraints enforcements
    // basically might have to check myself if the site is unique or not
    String site;

    // need to figure out the annotation and necessary stuff to make it work properly with the MongoDB database
//    Subscription subscription;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> roleTokens;

    private int serializeSensitiveCount;

    private Map<String, String> roleTokensHashed;


    public Company(String id, String site,  Map<String, String> roleTokens) {
        this.id = id;
        // generate id uses the 25-based site id
        this.siteId = generateSiteId();
        // make sure to increment the count
        COMPANY_COUNT += 1;
        this.site = site;

//      this.subscription = subscription;
        this.setRoleTokens(roleTokens);
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
            serializeSensitiveCount += 1;
            return this.id;
        }
        return null;
    }


    // not sure if the "value" field in the annotation refers to the field in the class
    // or the name displayed in the resulting Json
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

//    public Subscription getSubscription() {
//        return subscription;
//    }
//
//    public void setSubscription(Subscription subscription) {
//        this.subscription = subscription;
//    }

    public Map<String, String> getRoleTokens() {
        return this.roleTokensHashed;
    }

    public void setRoleTokens(Map<String, String> roleTokens) {
        this.roleTokens = roleTokens;

        // deep Copy the role Tokens
        this.roleTokensHashed = new HashMap<>(this.roleTokens);
        // hash the tokens
        for (Map.Entry<String, String> entry : this.roleTokensHashed.entrySet()) {
            entry.setValue(
                    String.valueOf(entry.getKey().hashCode()));
        }
    }
}


//@Configuration I still do not understand why it is not necessary to associate the Repository interface
// with the @Configuration / @Bean annotations. (maybe because it is an interface / abstract class)
interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findById(String id);
    Optional<Company> findBySite(String site);
}


