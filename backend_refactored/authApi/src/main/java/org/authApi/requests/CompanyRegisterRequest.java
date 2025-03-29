package org.authApi.requests;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for company registration")
public record CompanyRegisterRequest (
    @Schema(description = "Unique identifier for the company", example = "company_12345")
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String id,

    @Schema(description = "Company name", example = "Example Company")
    @NotBlank
    String companyName,

    @Schema(description = "Physical address of the company", example = "123 Business Street, Suite 100")
    @NotBlank
    String companyAddress,

    @Schema(description = "Top level domain for the company website", example = "www.example.com")
    @NotBlank
    @Pattern(regexp = "(www\\.)[a-zA-Z0-9_-]+\\.[a-zA-Z]{2,}", message = "the domain is expected to start with www. and end with at least a 2 character top level domain e.g ('org', 'edu', 'eu'...)")
    String topLevelDomain,

    @Schema(description = "Email address of the company owner", example = "owner@example.com")
    @NotBlank
    // expecting a valid email
    @Email(message = "Please provide a valid email address")
    // the pattern rejects standard email providers with any valid TLD (minimum 2 chars)
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@(?!gmail\\.[a-zA-Z]{2,}$|yahoo\\.[a-zA-Z]{2,}$|outlook\\.[a-zA-Z]{2,}$|hotmail\\.[a-zA-Z]{2,}$|aol\\.[a-zA-Z]{2,}$|protonmail\\.[a-zA-Z]{2,}$|icloud\\.[a-zA-Z]{2,}$|mail\\.[a-zA-Z]{2,}$|zoho\\.[a-zA-Z]{2,}$|yandex\\.[a-zA-Z]{2,}$|gmx\\.[a-zA-Z]{2,}$|live\\.[a-zA-Z]{2,}$|msn\\.[a-zA-Z]{2,}$)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$",
        message = "Please provide a valid company email. Standard email providers (gmail, yahoo, outlook, etc.) are not accepted regardless of TLD"
    )
    String ownerEmail,

    @Schema(description = "Email domain for company employees (can be null)", example = "example.com")
    // the main domain of a company can be null.
    @NotEmpty
    String mailDomain,

    @Schema(description = "Subscription tier for the company", example = "TIER_1", allowableValues = {"TIER_1", "TIER_2", "TIER_3"})
    @NotBlank
    String subscription
    ) {};





