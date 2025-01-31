package org.api.exceptions;


public class UserExceptions {
    public static class UserWithNoCompanyException extends RuntimeException {
        public UserWithNoCompanyException(String message) {
            super(message);
        }
    }

    public static class IncorrectRoleTokenException extends RuntimeException {
        public IncorrectRoleTokenException(String message) {
            super(message);
        }
    }

    public static class AlreadyExistingUserException extends RuntimeException {
        public AlreadyExistingUserException(String message) {
            super(message);
        }
    }
}

