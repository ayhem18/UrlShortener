package org.companyManagerApi.exceptions;

public class CompanyMngExceptions {
    public static class SameSubscriptionInUpdateException extends RuntimeException {
        public SameSubscriptionInUpdateException() {
            super("The subscription is the same as the current one");
        }
    }
}
