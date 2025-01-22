package org.api.exceptions;

public class ExistingCompanyException extends RuntimeException {
    public ExistingCompanyException(String message) {
        super(message);
    }

    public static class UserCompanyMisalignedException extends RuntimeException{
        public UserCompanyMisalignedException(String message) {
            super(message);
        }
    }
}
