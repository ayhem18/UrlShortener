package com.url_shortener.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

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

    @Id
    String id; // some sort of Company identifier

    @Column(nullable = false, unique = true) // the site must be unique as well
    String site;

    // need to figure out the annotation and necessary stuff to make it work properly with the MongoDB database
    Subscription subscription;

    List<String> RoleTokens;

    public Company(String site, Subscription subscription, List<String> roleTokens) {
        // generate id uses the 25-based id (instead of 26)
        this.id = generateId();
        // make sure to increment the count

        COMPANY_COUNT += 1;
        this.site = site;
        this.subscription = subscription;
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

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public List<String> getRoleTokens() {
        return RoleTokens;
    }

    public void setRoleTokens(List<String> roleTokens) {
        RoleTokens = roleTokens;
    }

}
