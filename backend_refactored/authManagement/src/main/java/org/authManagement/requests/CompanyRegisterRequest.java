package org.authManagement.requests;

import jakarta.validation.constraints.*;

public record CompanyRegisterRequest (
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String id,

    @NotBlank
    String companyName,

    @NotBlank
    String companyAddress,

    @NotBlank
    @Pattern(regexp = "(www\\.)[a-zA-Z0-9_-]+\\.[a-zA-Z]{2,}", message = "the domain is expected to start with www. and end with at least a 2 character top level domain e.g ('org', 'edu', 'eu'...)")
    String topLevelDomain,


    @NotBlank
    // expecting a valid email
    @Email(message = "Please provide a valid email address")
    // the pattern rejects standard email providers with any valid TLD (minimum 2 chars)
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@(?!gmail\\.[a-zA-Z]{2,}$|yahoo\\.[a-zA-Z]{2,}$|outlook\\.[a-zA-Z]{2,}$|hotmail\\.[a-zA-Z]{2,}$|aol\\.[a-zA-Z]{2,}$|protonmail\\.[a-zA-Z]{2,}$|icloud\\.[a-zA-Z]{2,}$|mail\\.[a-zA-Z]{2,}$|zoho\\.[a-zA-Z]{2,}$|yandex\\.[a-zA-Z]{2,}$|gmx\\.[a-zA-Z]{2,}$|live\\.[a-zA-Z]{2,}$|msn\\.[a-zA-Z]{2,}$)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
        message = "Please provide a valid company email. Standard email providers (gmail, yahoo, outlook, etc.) are not accepted regardless of TLD"
    )
    String ownerEmail,

    // the main domain of a company can be null.
    @NotEmpty
    String mailDomain,

    @NotBlank
    String subscription
    ) {};





