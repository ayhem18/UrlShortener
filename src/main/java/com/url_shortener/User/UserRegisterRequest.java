package com.url_shortener.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest (
        @NotBlank
        @Size(min=8, max=16, message="The length of id is expected to be between 8 and 16")
        String companyId,

        @Size(min=2, max=16, message="username must of length between 2 and 16")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_$@]+",
                message="Only alpha numerical characters are allowed + '_'. The first character must be alphabetic")
        @NotBlank
        String username,

        @NotBlank
        String password,

        @NotBlank
        String role,

        @NotBlank
        String roleToken) {};


/**
 * user-register related exceptions
 */

class UserWithNoCompanyException extends RuntimeException {
        public UserWithNoCompanyException(String message) {
                super(message);
        }
}

class UndefinedRoleException extends RuntimeException {
        public UndefinedRoleException(String message) {
                super(message);
        }
}

class IncorrectRoleTokenException extends RuntimeException {
        public IncorrectRoleTokenException(String message) {
                super(message);
        }
}
