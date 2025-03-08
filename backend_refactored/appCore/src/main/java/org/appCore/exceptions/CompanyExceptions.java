package org.appCore.exceptions;

public class CompanyExceptions {
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
}
