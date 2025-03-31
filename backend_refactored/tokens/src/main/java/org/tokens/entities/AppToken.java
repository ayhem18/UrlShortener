package org.tokens.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.access.Role;
import org.company.entities.Company;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("tokens")
@Schema(description = "Token entity used for user authentication and verification")
public class AppToken {
    public final static String TOKEN_CLASS_NAME = "AppToken";

    @Schema(description = "Possible states of a token")
    public enum TokenState {
        ACTIVE,
        INACTIVE,
        EXPIRED
    }

    @Id
    @Schema(description = "Unique identifier for the token", example = "token_5f3e2a1b", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String tokenId;

    @Schema(description = "Hashed token value for verification", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String tokenHash;

    @Schema(description = "Current state of the token", example = "ACTIVE")
    private TokenState tokenState;

    @Schema(description = "Time when the token expires (null for non-expiring tokens)", example = "2023-12-31T23:59:59")
    private LocalDateTime expirationTime;
    
    @Schema(description = "Company this token is associated with")
    @DocumentReference
    private Company company;
    
    @Schema(description = "Role this token grants")
    private Role role;

    @Schema(description = "Time when the token was created", example = "2023-12-31T23:59:59")
    private LocalDateTime createdAt;

    public AppToken(String tokenId, String tokenHash, Company company, Role role, LocalDateTime expirationTime) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.company = company;
        this.role = role;

        this.tokenState = TokenState.INACTIVE;
        this.expirationTime = expirationTime;

        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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

    @JsonGetter("createdAt")
    public String getCreatedAtJackson() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(createdAt);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    @SuppressWarnings("unused")
    private void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

