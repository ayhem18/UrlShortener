package org.api.controllers.user;

class UserWithNoCompanyException extends RuntimeException {
    public UserWithNoCompanyException(String message) {
        super(message);
    }
}

class IncorrectRoleTokenException extends RuntimeException {
    public IncorrectRoleTokenException(String message) {
        super(message);
    }
}

class UserBeforeOwnerException extends RuntimeException {
    public UserBeforeOwnerException(String message) {
        super(message);
    }
}

class MultipleOwnersException extends RuntimeException {
    public MultipleOwnersException(String message) {
        super(message);
    }
}

class AlreadyExistingUserException extends RuntimeException {
    public AlreadyExistingUserException(String message) {
        super(message);
    }

}
