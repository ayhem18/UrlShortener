package org.authManagement.requests;

import jakarta.validation.constraints.*;
import org.authManagement.validation.AllowNullButNotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for user registration")
public record UserRegisterRequest (
        @Schema(description = "Email address of the user", example = "user@example.com")
        @NotBlank
        @Email(message = "Please provide a valid email address")
        String email,

        @Schema(description = "Username for login", example = "johnsmith")
        @NotBlank
        @Size(min=2, max=16, message="username must of length between 2 and 16")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_$@]+",
                message="Only alpha numerical characters are allowed + '_'. The first character must be alphabetic")
        String username,

        @Schema(description = "User password", example = "password123")
        @NotBlank
        @Size(min=8, max=16, message="password must of length between 8 and 16")
        String password,

        @Schema(description = "User's first name", example = "John")
        @NotBlank
        String firstName,

        @Schema(description = "User's last name", example = "Smith")
        @NotBlank
        String lastName,

        @Schema(description = "User's middle name (optional)", example = "Robert")
        @AllowNullButNotEmpty(message = "middleName can be null but not empty")
        String middleName,

        @Schema(description = "Company ID the user belongs to", example = "company_12345")
        @NotBlank
        @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
        String companyId,

        @Schema(description = "User role in the company", example = "employee", allowableValues = {"owner", "admin", "employee"})
        @NotBlank
        String role,

        @Schema(description = "Token for role validation (not required for owner role)", example = "token_5f3e2a1b")
        @AllowNullButNotEmpty(message = "roleToken can be null but not empty")
        String roleToken) {};


