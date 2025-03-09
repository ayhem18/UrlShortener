package org.appCore.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest (
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String id,

    @NotBlank
    @Pattern(regexp = "(www\\.)*[a-zA-Z0-9]+\\.[a-zA-Z]{2,}", message = "the domain is in the wrong format")
    String topLevelDomain,

    @NotBlank
    String subscription,

    @NotBlank
    // expecting a valid email
    @Email(message = "Please provide a valid email address")
    // expecting a valid company email. Standard email providers (gmail, yahoo, outlook, etc.) are not accepted
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@(?!gmail\\.com$|yahoo\\.(com|fr)$|outlook\\.com$|hotmail\\.com$|aol\\.com$|protonmail\\.com$|icloud\\.com$|mail\\.com$|zoho\\.com$|yandex\\.com$|gmx\\.com$|live\\.com$|msn\\.com$)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
        message = "Please provide a valid company email. Standard email providers (gmail, yahoo, outlook, etc.) are not accepted"
    )
    String ownerEmail,

    // the main domain of a company can be null.
    String mailDomain

) {};





