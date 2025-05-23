package org.tokenApi.exceptions;

/**
 * Custom exceptions for token-related operations
 */
public class TokenExceptions {
    /**
     * Thrown when a user does not have sufficient role privileges
     */
    public static class InsufficientRoleAuthority extends RuntimeException {
        public InsufficientRoleAuthority(String message) {
            super(message);
        }
    }

    /**
     * Thrown when the maximum number of tokens has been reached
     */
    public static class NumTokensLimitExceeded extends RuntimeException {
        public NumTokensLimitExceeded(String message) {
            super(message);
        }
    }


    /**
     * Thrown when a user is not found
     */
    public static class RevokedUserNotFoundException extends RuntimeException {
        public RevokedUserNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class NoUserTokenLinkException extends RuntimeException {
        public NoUserTokenLinkException(String message) {
            super(message);
        }
    }
}
