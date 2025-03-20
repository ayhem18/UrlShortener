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
    // login credentials
    @Id
    private String email;

    private String username;

    // the password should be written :Json to obj, but not read: obj to json
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // user personal information
    private String firstName;

    private String lastName;

    private String middleName;


    // user company information
    // each user is associated with a company (a specific site)
    @DocumentReference
    private Company company;

    // the role the user plays in this company (determines the authorities !!)
    private Role role;

    // user extra information for functional purposes 
    private long urlEncodingCount;


    public AppUser(String email, String userName, String password, String firstName, String lastName, String middleName, Company company, Role role) {
        this.email = email;
        this.username = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.company = company;
        this.role = role;
        this.urlEncodingCount = 0; // starts at 0...
    }

    // the no-argument constructor is needed for Jackson de/serialization
    @SuppressWarnings("unused")
    private AppUser() {
    }


    ///////////////////////////////// Getters /////////////////////////////////////////////
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
    public String getMiddleName() {
        return middleName;
    }

    public long getUrlEncodingCount() {
        return urlEncodingCount;
    }

    // can set password
    public String getPassword() {
        return password;
    }

    public Company getCompany() {
        return company;
    }

    public Role getRole() {
        return role;
    }


    ///////////////////////////////// Setters /////////////////////////////////////////////
    // can set first name
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // can set last name
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // can set middle name
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    // can set username
    public void setUsername(String userName) {
        this.username = userName;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    // can set role
    public void setRole(Role role) {
        this.role = role;
    }


    ///////////////////////////////// Jackson Setters /////////////////////////////////////////////
    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
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
