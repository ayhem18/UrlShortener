package org.data.entities;

/*
* At the time of writing this code (27.01.2024), my experience suggests that classes that represent
* collections are indeed tricky to work with: I still do not have a very thorough and clear understanding
* of the underlying processing. What seems to work so far is having a simple Record-like class whose sole purpose
* is saving data. This might be tricky with data class that requires additional logic such as Company.
*
* The best solution I could think of is building a wrapper around a very minimalistic Company class (Record-like)
* and cut all means of modifying the object directly; leaving CompanyWrapper as the sole way to interacting
* with the object. The simplicity of the class simplifies working with the database while the wrapper guarantees:

1. the siteId is generated correctly (without having the logic inside the Company class)
2. the roleTokens and hashedRoteTokens are consistent
* */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.common.Subscription;
import org.data.repositories.CompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

import java.util.HashMap;
import java.util.Map;

public class CompanyWrapper {
    private final Company company;

    public CompanyWrapper(String id,
                          String site,
                          Subscription sub,
                          Map<String, String> roleTokens,
                          PasswordEncoder encoder,
                          CustomGenerator gen,
                          long documentCount
                          ) {


        Map<String, String> hashedTokens = prepareHashedTokens(roleTokens, encoder);
        String siteId = gen.generateId(documentCount);

        // now we are ready for initializing the company object
        this.company = new Company(id, site, siteId, roleTokens, hashedTokens, sub);
    }

    private Map<String, String> prepareHashedTokens(Map<String, String> roleTokens, PasswordEncoder encoder) {
        // deep Copy the role Tokens
        Map<String, String> roleTokensHashed = new HashMap<>(roleTokens);

        // hash the tokens
        for (Map.Entry<String, String> entry : roleTokens.entrySet()) {
            entry.setValue(encoder.encode(entry.getValue())); // make sure to encode the value !! and not the key !!!
        }

        return roleTokensHashed;
    }

    ///////////////////////////////// GETTERS /////////////////////////////////////////////
    public Map<String, String> getTokens() {
        return this.company.getRoleTokensHashed();
    }

    public Subscription getSubscription() {
        return this.company.getSubscription();
    }

    public String getSite() {
        return this.company.getSite();
    }

    public String getSiteId() {
        return this.company.getSiteId();
    }


    ///////////////////////////////// SETTERS /////////////////////////////////////////////
    void setSite(String site) {
        this.company.setSite(site);
    }

    void setId(String id) {
        this.company.setId(id);
    }

    // the subscription can be changed
    void setSubscription(Subscription subscription) {
        this.company.setSubscription(subscription);
    }

    void setRoleTokens(Map<String,String> roleTokens, PasswordEncoder encoder) {
        Map<String, String> hashedTokens = prepareHashedTokens(roleTokens, encoder);
        this.company.setRoleTokens(roleTokens);
        this.company.setRoleTokensHashed(hashedTokens);
    }

    ///////////////////////////////// OTHER /////////////////////////////////////////////
    public void save(CompanyRepository repo) {
        repo.save(this.company);
    }

    public String serialize(ObjectMapper mapper) throws JsonProcessingException {
        return mapper.writeValueAsString(this.company);
    }
}

