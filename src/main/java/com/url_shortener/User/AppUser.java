package com.url_shortener.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("user")
public class AppUser {

    @Id
    @JsonProperty(value = "user_name")
    private String userName;

    // the password should be written :Json to obj
    // but not read: obj to json
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // each user is associated with a company (a specific site)
    Company company;

    // the role the user plays in this company (determines the authorities !!)
    Role role;

    public AppUser(String userName, String password, Company company, Role role) {
        this.userName = userName;
        this.password = password;
        this.company = company;
        this.role = role;
    }

    public AppUser() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
