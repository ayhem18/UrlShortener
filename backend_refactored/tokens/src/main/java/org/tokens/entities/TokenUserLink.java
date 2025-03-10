package org.tokens.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.user.entities.AppUser;

@Document("token_user_links")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenUserLink {
    public final static String TOKEN_USER_LINK_CLASS_NAME = "TOKEN_USER_LINK";

    @Id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @DocumentReference
    private AppToken token;

    @DocumentReference
    private AppUser user;

    private LocalDateTime linkActivationTime;

    private LocalDateTime linkDeactivationTime;
    

    public TokenUserLink(AppToken token, AppUser user) {
        // few checks to make sure the link is valid
        // 1. the token must be activate
        // 2. the role of the token and the role of the user must match
        // 3. the token company id must match the user company id
        if (!token.getTokenState().equals(AppToken.TokenState.ACTIVE)) {
            throw new IllegalStateException("Token is not active");
        }
        
        if (!token.getRole().equals(user.getRole())) {
            throw new IllegalStateException("Role of the token and the role of the user do not match");
        }
        
        if (!token.getCompany().getId().equals(user.getCompany().getId())) {
            throw new IllegalStateException("Token and user must be from the same company");
        }

        this.token = token;
        this.user = user; 

        this.linkActivationTime = LocalDateTime.now();
        this.linkDeactivationTime = null;
    }


    public void deactivate() {
        if (this.linkDeactivationTime != null) {
            throw new IllegalStateException("Link is already deactivated");
        }
        this.linkDeactivationTime = LocalDateTime.now();
    }

    // Private no-argument constructor
    @SuppressWarnings("unused")
    private TokenUserLink() {
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public AppToken getToken() {
        return token;
    }
    
    public AppUser getUser() {
        return user;
    }
    
    public LocalDateTime getLinkActivationTime() {
        return linkActivationTime;
    }
    
    public LocalDateTime getLinkDeactivationTime() {
        return linkDeactivationTime;
    }
    
    // Private setters
    @SuppressWarnings("unused")
    private void setId(String id) {
        this.id = id;
    }
    
    @SuppressWarnings("unused")
    private void setToken(AppToken token) {
        this.token = token;
    }
    
    @SuppressWarnings("unused")
    private void setUser(AppUser user) {
        this.user = user;
    }
    
    @SuppressWarnings("unused")
    private void setLinkActivationTime(LocalDateTime linkActivationTime) {
        this.linkActivationTime = linkActivationTime;
    }
    
    @SuppressWarnings("unused")
    private void setLinkDeactivationTime(LocalDateTime linkDeactivationTime) {
        this.linkDeactivationTime = linkDeactivationTime;
    }
}
