package org.authManagement.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for company verification")
public record CompanyVerifyRequest(
    @Schema(description = "Unique identifier for the company", example = "company_12345")
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String companyId,
    
    @Schema(description = "Verification token sent to the owner's email", example = "token_5f3e2a1b")
    @NotBlank
    String token,
    
    @Schema(description = "Email address of the company owner", example = "owner@example.com")
    @NotBlank
    @Email(message = "Please provide a valid email address")
    String email
) {} 