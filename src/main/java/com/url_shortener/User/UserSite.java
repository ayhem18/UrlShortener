package com.url_shortener.User;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// this class represents the main user of our service


// IMPORTANT: MAKE SURE TO PASS THE @Entity annotation so that JPA recognizes it as a database TABLE
@Entity
public class UserSite {

    @JsonProperty("user_name")
    private String userName;

    // EACH entity must have at least one field with the @Id annotation
    @Id
    private String site;

    // the password should be written :Json to obj
    // but not read: obj to json
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    public UserSite(String userName, String site, String password) {
        this.userName = userName;
        this.site = site;
        this.password = password;
    }

    public UserSite() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
