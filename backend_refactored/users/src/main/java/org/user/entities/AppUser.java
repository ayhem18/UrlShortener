package org.user.entities;

import org.access.Role;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.company.entities.Company;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document()
public class AppUser {
    @Id
    private String email;

    private String username;

    // the password should be written :Json to obj, but not read: obj to json
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // each user is associated with a company (a specific site)
    @DocumentReference
    private Company company;

    // the role the user plays in this company (determines the authorities !!)
    private Role role;

    public AppUser(String email, String userName, String password, Company company, Role role) {
        this.email = email;
        this.username = userName;
        this.password = password;
        this.company = company;
        this.role = role;
    }

    // the no-argument constructor is needed for Jackson de/serialization
    @SuppressWarnings("unused")
    private AppUser() {
    }

    @JsonGetter("role")
    private String jsonGetRole() {
        return role.toString();
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
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

    public Role getRole() {
        return role;
    }

    // private setters for jackson

    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "username= '" + username + '\'' +
                ", company= " + company.getId() +
                ", role= " + role +
                '}';
    }
}
