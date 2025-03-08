package org.appCore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonGetter;
import org.access.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Document("Token")
@JsonInclude(JsonInclude.Include.NON_NULL) // a class-wide annotation Making Jackson ignore all null fields
public class Token {
    public static final String TOKEN_COLLECTION_NAME = "TOKEN";

    @Id
    @JsonIgnore
    private String id;

    private String token;

    // Remove the existing JsonProperty annotation
    private String tokenHash;

    // Add a counter for serialization control
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int serializeSensitiveCount = 0;

    private Role role;

    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;

    @DocumentReference
    private Company company;

    // Private no-argument constructor
    @SuppressWarnings("unused")
    private Token() {
    }

    public Token(String id, 
                String token,
                String tokenHash,
                Company company,
                Role role,
                LocalDateTime createdAt,
                LocalDateTime expiresAt) {
        this.id = id;
        this.token = token;
        this.tokenHash = tokenHash;
        this.company = company;
        this.role = role;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Add JsonGetter for tokenHash
    @JsonGetter(value = "tokenHash")
    private String jsonGetTokenHash() {
        if (this.serializeSensitiveCount < 1) {
            this.serializeSensitiveCount += 1;
            return this.tokenHash;
        }
        return null;
    }

    // Public getters
    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Company getCompany() {
        return company;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    // Add getter for serializeSensitiveCount
    @SuppressWarnings("unused")
    private int getSerializeSensitiveCount() {
        return serializeSensitiveCount;
    }

    // Add private setter for serializeSensitiveCount for Jackson serialization
    @SuppressWarnings("unused")
    private void setSerializeSensitiveCount(int serializeSensitiveCount) {
        this.serializeSensitiveCount = serializeSensitiveCount;
    }

    // Private setters
    @SuppressWarnings("unused")
    private void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    private void setToken(String token) {
        this.token = token;
    }

    @SuppressWarnings("unused")
    private void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }

    @SuppressWarnings("unused")
    private void setRole(Role role) {
        this.role = role;
    }

    @SuppressWarnings("unused")
    private void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }   

    @SuppressWarnings("unused")
    private void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
