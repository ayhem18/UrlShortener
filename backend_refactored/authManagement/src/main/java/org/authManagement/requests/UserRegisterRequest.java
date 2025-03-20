package org.authManagement.requests;

import jakarta.validation.constraints.*;

public record UserRegisterRequest (
        @NotBlank
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank
        @Size(min=2, max=16, message="username must of length between 2 and 16")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_$@]+",
                message="Only alpha numerical characters are allowed + '_'. The first character must be alphabetic")
        String username,

        @NotBlank
        String password,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotEmpty // middle name can be set to null but not an empty string
        String middleName,

        @NotBlank
        @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
        String companyId,

        @NotBlank
        String role,

        @NotEmpty // can be set to Null but not an empty string
        String roleToken) {};


