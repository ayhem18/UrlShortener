package org.api.exceptions;

public class CompanyUniquenessConstraints{
    public static class ExistingCompanyException extends RuntimeException{
        public ExistingCompanyException(String message) {
            super(message);
        }
    }

    public static class UserCompanyMisalignedException extends RuntimeException{
        public UserCompanyMisalignedException(String message) {
            super(message);
        }
    }

    public static class ExistingSiteException extends RuntimeException{
        public ExistingSiteException(String message) {
            super(message);
        }
    }
}
