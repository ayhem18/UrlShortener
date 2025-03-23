package org.user.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;


@Document("urlEncodings")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlEncoding {

    
    @DocumentReference 
    private AppUser user;

    private String url;

    private String urlEncoded;

    private int urlEncodingCount;

    private LocalDateTime urlEncodingTime;

    public UrlEncoding(AppUser user, String url, String urlEncoded) {
        this.user = user;
        this.url = url;
        this.urlEncoded = urlEncoded;
        this.urlEncodingCount = 0;
        this.urlEncodingTime = LocalDateTime.now();
    }



    // private no-arg constructor for Jackson
    @SuppressWarnings("unused")
    private UrlEncoding() {
        this.urlEncodingCount = 0;
        this.urlEncodingTime = LocalDateTime.now();
    }
    

    // private setters for Jackson
    @SuppressWarnings("unused")
    private void setUser(AppUser user) {
        this.user = user;
    } 

    @SuppressWarnings("unused") 
    private void setUrl(String url) {
        this.url = url;
    }

    @SuppressWarnings("unused")
    private void setUrlEncoded(String urlEncoded) {
        this.urlEncoded = urlEncoded;
    }
    
    
    @SuppressWarnings("unused")
    private void setUrlEncodingCount(int urlEncodingCount) {
        this.urlEncodingCount = urlEncodingCount;
    }

    @SuppressWarnings("unused")
    private void setUrlEncodingTime(LocalDateTime urlEncodingTime) {
        this.urlEncodingTime = urlEncodingTime;
    }
    
    // some useful getters
    public AppUser getUser() {
        return user;
    }

    public String getUrl() {
        return url;
    }   

    public String getUrlEncoded() {
        return urlEncoded;
    }

    public int getUrlEncodingCount() {
        return urlEncodingCount;
    }   
    
    public LocalDateTime getUrlEncodingTime() {
        return urlEncodingTime;
    }
}

