package org.appCore.exceptions;

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

    public static class ExistingCompanyException extends RuntimeException{
        public ExistingCompanyException(String message) {
            super(message);
        }
    }

    public static class ExistingSiteException extends RuntimeException{
        public ExistingSiteException(String message) {
            super(message);
        }
    }

    public static class NoCompanyException extends RuntimeException {
        public NoCompanyException(String message) {
            super(message);
        }
    }

    public static class CompanyUserMailMisalignmentException extends RuntimeException {
        public CompanyUserMailMisalignmentException(String message) {
            super(message);
        }
    }
}
