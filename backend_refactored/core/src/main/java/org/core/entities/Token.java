package org.core.entities;

import org.access.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Token")
public class Token {
    @Id
    private String id;

    private String token;

    private String tokenHash;

    private Role role;

    private Date createdAt;
    
    private Date expiresAt;

    @DocumentReference
    private Company company;

    // Private no-argument constructor
    private Token() {
    }

    // Public constructor with all fields except id (which will be auto-generated)
    public Token(String token, String tokenHash, String companyId, Role role, Date createdAt, Date expiresAt) {
        this.token = token;
        this.tokenHash = tokenHash;
        this.companyId = companyId;
        this.role = role;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
    
    // Constructor with id (for internal/testing use)
    public Token(String id, String token, String tokenHash, String companyId, Role role, Date createdAt, Date expiresAt) {
        this.id = id;
        this.token = token;
        this.tokenHash = tokenHash;
        this.company = company;
        this.role = role;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
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

    public String getCompanyId() {
        return companyId;
    }

    public Role getRole() {
        return role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    
    public Date getExpiresAt() {
        return expiresAt;
    }

    // Private setters
    private void setId(String id) {
        this.id = id;
    }

    private void setToken(String token) {
        this.token = token;
    }

    private void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    private void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    private void setRole(Role role) {
        this.role = role;
    }

    private void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    private void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
