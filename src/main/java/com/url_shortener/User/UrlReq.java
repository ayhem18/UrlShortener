package com.url_shortener.User;

import jakarta.validation.constraints.NotBlank;

public class UrlReq {
    // still unable to customize the error message when passing a blank url
    @NotBlank(message="The url cannot be empty")
    private String url;

    public UrlReq(String url) {
        this.url = url;
    }

    // every class that goes through Jackson needs a no-arg constructor
    public UrlReq() {

    }

    // since the Jackson library does not set the fields using the constructor, it is necessary
    // to add a setter (otherwise, the Parser would have no access to the field)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
