package org.tokens.entities;

import java.util.Random;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.user.entities.AppUser;

@Document("user_token_links")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenUserLink {
    public final static String TOKEN_USER_LINK_CLASS_NAME = "TOKEN_USER_LINK";

    @Id
    private String id;

    @DocumentReference
    private Token token;

    @DocumentReference
    private AppUser user;

    private LocalDateTime activationTime;

    private LocalDateTime deactivationTime;
    
    // Private no-argument constructor
    private TokenUserLink() {
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public Token getToken() {
        return token;
    }
    
    public AppUser getUser() {
        return user;
    }
    
    public LocalDateTime getActivationTime() {
        return activationTime;
    }
    
    public LocalDateTime getDeactivationTime() {
        return deactivationTime;
    }
    
    // Private setters
    private void setId(String id) {
        this.id = id;
    }
    
    private void setToken(Token token) {
        this.token = token;
    }
    
    private void setUser(AppUser user) {
        this.user = user;
    }
    
    private void setActivationTime(LocalDateTime activationTime) {
        this.activationTime = activationTime;
    }
    
    private void setDeactivationTime(LocalDateTime deactivationTime) {
        this.deactivationTime = deactivationTime;
    }
}
