package org.tokens;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("tokens")
public class Token {
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
    
    // Private no-argument constructor
    private Token() {
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
    

    // Private setters for jackson
    private void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    
    private void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    private void setTokenState(TokenState tokenState) {
        this.tokenState = tokenState;
    }
}


