package org.appCore.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyVerifyRequest(
    @NotBlank
    @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
    String companyId,
    
    @NotBlank
    String token,
    
    @NotBlank
    @Email(message = "Please provide a valid email address")
    String email
) {} 