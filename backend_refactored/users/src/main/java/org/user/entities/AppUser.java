package org.user.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.access.Role;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.company.entities.Company;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import io.swagger.v3.oas.annotations.media.Schema;


@Document()
@Schema(description = "User entity representing a system user")
@SuppressWarnings("unused")
public class AppUser {
    // login credentials
    @Id
    @Schema(description = "Email address of the user (primary identifier)", example = "user@example.com")
    private String email;

    @Schema(description = "Username for login", example = "john10277")
    private String username;

    // the password should be written :Json to obj, but not read: obj to json
    @Schema(description = "Hashed password", example = "$2a$10$...", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // user personal information
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Smith")
    private String lastName;

    @Schema(description = "User's middle name (optional)", example = "Robert")
    private String middleName;

    @Schema(description = "Timestamp when user joined", example = "2023-07-15T14:30:00")
    private LocalDateTime timeJoined;

    // user company information
    // each user is associated with a company (a specific site)
    @Schema(description = "Company the user belongs to")
    @DocumentReference
    private Company company;

    // the role the user plays in this company (determines the authorities !!)
    @Schema(description = "User's role in the company")
    private Role role;

    // user extra information for functional purposes 
    @Schema(description = "Count of URL encodings performed by this user", example = "42")
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
        this.timeJoined = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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

    @JsonGetter(value="timeJoined")
    public String getTimeJoinedJackson() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(timeJoined);
    }

    public LocalDateTime getTimeJoined() {
        return timeJoined;
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

    public void incrementUrlEncodingCount() {
        this.urlEncodingCount += 1;
    }   

    ///////////////////////////////// Jackson Setters /////////////////////////////////////////////
    @SuppressWarnings("unused")
    private void setUrlEncodingCount(long urlEncodingCount) {
        this.urlEncodingCount = urlEncodingCount;
    }

    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }

    @SuppressWarnings("unused")
    private void setTimeJoined(LocalDateTime joinTime) {
        this.timeJoined = joinTime;
    }


    @Override
    public String toString() {
        return "AppUser{" +
                "username= '" + username + '\'' +
                ", company= " + company.getId() +
                ", role= " + role +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AppUser)) {
            return false;
        }
        return ((AppUser) obj).getEmail().equalsIgnoreCase(this.getEmail());
    }
}
