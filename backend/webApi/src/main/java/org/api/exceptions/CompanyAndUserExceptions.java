package org.api.exceptions;

public class CompanyAndUserExceptions {
    public static class UserCompanyMisalignedException extends RuntimeException{
        public UserCompanyMisalignedException(String message) {
            super(message);
        }
    }

    public static class UserBeforeOwnerException extends RuntimeException {
        public UserBeforeOwnerException(String message) {
            super(message);
        }
    }

    public static class MultipleOwnersException extends RuntimeException {
        public MultipleOwnersException(String message) {
            super(message);
        }
    }
}
