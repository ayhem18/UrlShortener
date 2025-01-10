package com.url_shortener.User;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest (
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String id,

    @NotBlank
    @Pattern(regexp = "(www\\.)*[a-zA-Z0-9]+\\.[a-zA-Z]{2,}", message = "the site is in the wrong format")
    String site) {};



class ExistingCompanyException extends RuntimeException {
    public ExistingCompanyException(String message) {
        super(message);
    }
}

