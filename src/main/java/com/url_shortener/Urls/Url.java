package com.url_shortener.Urls;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

class UrlRequest {
    @NotBlank
    private String url;

    public UrlRequest(String url) {
        this.url = url;
    }

    // every class that goes through Jackson needs a no-arg constructor
    public UrlRequest() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}



@Entity
public class Url {
    @Id
    private String url;
    @JsonProperty("url_short")
    private String hash;


    public Url(String url, String hash) {
        this.url = url;
        this.hash = hash;
    }

    public Url(String url) {
        this(url, null);
    }

    public Url() {
    }

    public String getUrl() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}

// create a repository to save and retrieve url records <ObjectClass, id Type>
interface UrlRepository extends CrudRepository<Url, String> {
    Optional<Url> findByUrl(String url);
}

