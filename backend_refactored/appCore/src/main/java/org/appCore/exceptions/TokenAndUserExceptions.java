package org.appCore.exceptions;

public class TokenAndUserExceptions {
    public static class TokenAlreadyUsedException extends RuntimeException {
        public TokenAlreadyUsedException(String message) {
            super(message);
        }
    }

    public static class MissingTokenException extends RuntimeException {
        public MissingTokenException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }

    public static class TokenUserMismatchException extends RuntimeException {
        public TokenUserMismatchException(String message) {
            super(message);
        }
    }

    public static class TokenVerificationFailedException extends RuntimeException {
        public TokenVerificationFailedException(String message) {
            super(message);
        }
    }

    public static class TokenNotFoundForRoleException extends RuntimeException {
        public TokenNotFoundForRoleException(String message) {
            super(message);
        }
    }
}
