package com.url_shortener.Urls;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


@Entity
public class Url {
    @Id
    private String url;

    // setting the non-null and unique constraints to the field
    @Column(nullable = false, unique = true)
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
    Optional<Url> findByHash(String hash);
}

