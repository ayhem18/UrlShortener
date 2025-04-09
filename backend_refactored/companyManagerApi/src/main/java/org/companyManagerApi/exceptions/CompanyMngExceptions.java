package org.companyManagerApi.exceptions;

public class CompanyMngExceptions {
    public static class SameSubscriptionInUpdateException extends RuntimeException {
        public SameSubscriptionInUpdateException(String message) {
            super(message);
        }
    }
}
