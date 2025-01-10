package com.url_shortener.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
public class Company {
    private static long COMPANY_COUNT = 0;

    private static String generateId() {
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

    @Column(nullable = false, unique = true) // the site must be unique as well
    String site;

    // need to figure out the annotation and necessary stuff to make it work properly with the MongoDB database
//    Subscription subscription;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Map<String, String> RoleTokens;

    public Company(String id, String site,  Map<String, String> roleTokens) {
        this.id = id;
        // generate id uses the 25-based site id
        this.siteId = generateId();
        // make sure to increment the count
        COMPANY_COUNT += 1;
        this.site = site;
//        this.subscription = subscription;
        RoleTokens = roleTokens;
    }

    public Company() {
    }

    @Override
    public String toString() {
        return "Company{" +
                "site='" + site + '\'' +
                '}';
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
        return RoleTokens;
    }

    public void setRoleTokens(Map<String, String> roleTokens) {
        RoleTokens = roleTokens;
    }
}


@Configuration
interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findById(String id);
    Optional<Company> findBySite(String site);
}


