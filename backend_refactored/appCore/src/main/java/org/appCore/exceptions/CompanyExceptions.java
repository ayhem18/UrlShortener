package org.appCore.exceptions;

public class CompanyExceptions {
    public static class ExistingCompanyException extends RuntimeException{
        public ExistingCompanyException(String message) {
            super(message);
        }
    }

    public static class ExistingTopLevelDomainException extends RuntimeException{
        public ExistingTopLevelDomainException(String message) {
            super(message);
        }
    }

    public static class NoCompanyException extends RuntimeException {
        public NoCompanyException(String message) {
            super(message);
        }
    }


    public static class CompanyAlreadyVerifiedException extends RuntimeException {
        public CompanyAlreadyVerifiedException(String message) {
            super(message);
        }
    }
}
