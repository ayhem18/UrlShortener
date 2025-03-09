package org.tokens.entities;

import java.time.LocalDateTime;

import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("tokens")
public class AppToken {
    public final static String TOKEN_CLASS_NAME = "AppToken";

    public enum TokenState {
        ACTIVE,
        INACTIVE,
        EXPIRED
    }

    @Id
    private String tokenId;

    // write only property  
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String tokenHash;

    private TokenState tokenState;

    private LocalDateTime expirationTime;
    
    @DocumentReference
    private Company company;
    
    private Role role;

    public AppToken(String tokenId, String tokenHash, Company company, Role role, LocalDateTime expirationTime) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.tokenState = TokenState.INACTIVE;
        this.expirationTime = expirationTime;
        this.company = company;
        this.role = role;
    }

    public AppToken(String tokenId, String tokenHash, Company company, Role role) {
        this(tokenId, tokenHash, company, role, null);
    }
    
    // Private no-argument constructor
    @SuppressWarnings("unused")
    private AppToken() {
    }

    public void activate() {
        if (this.tokenState != TokenState.INACTIVE) {
            throw new IllegalStateException("Only inactive tokens can be activated");
        }
        this.tokenState = TokenState.ACTIVE;
    }

    public void expire() {
        if (this.tokenState == TokenState.EXPIRED) {
            throw new IllegalStateException("Expired tokens cannot be made expired again");
        }
        this.tokenState = TokenState.EXPIRED;
    }

    // Getters
    public String getTokenId() {
        return tokenId;
    }
    
    public String getTokenHash() {
        return tokenHash;
    }
    
    public TokenState getTokenState() {
        return tokenState;
    }
    
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public Role getRole() {
        return role;
    }

    // Private setters for jackson
    @SuppressWarnings("unused")
    private void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    
    @SuppressWarnings("unused")
    private void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    @SuppressWarnings("unused")
    private void setTokenState(TokenState tokenState) {
        this.tokenState = tokenState;
    }

    @SuppressWarnings("unused")
    private void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
    
    @SuppressWarnings("unused")
    private void setCompany(Company company) {
        this.company = company;
    }
    
    @SuppressWarnings("unused")
    private void setRole(Role role) {
        this.role = role;
    }
}

