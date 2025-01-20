package org.api.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class UrlReq {
    // still unable to customize the error message when passing a blank url
    @NotBlank(message="The url cannot be empty")
    @Pattern(regexp = "http(s)?://(www\\.)*[a-zA-Z0-9]+\\.[a-zA-Z]{3,}(/+[a-zA-Z0-9?=_!.-]+)*",
            message = "The passed url is not valid")
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
