package org.urlService.exceptions;

public class UrlExceptions {
    public static class InvalidUrlException extends RuntimeException{
        public InvalidUrlException(String message) {
            super(message);
        }
    }

    public static class DailyLimitExceededException extends RuntimeException {
        public DailyLimitExceededException(String message) {
            super(message);
        }
    }

    public static class UrlCompanyDomainMistachException extends RuntimeException {
        public UrlCompanyDomainMistachException(String message) {
            super(message);
        }
    }


    public static class UrlCompanyDomainExpired extends RuntimeException {
        public UrlCompanyDomainExpired(String message) {
            super(message);
        }
    }

    public static class InvalidTopLevelDomainException extends RuntimeException {
        public InvalidTopLevelDomainException(String message) {
            super(message);
        }
    }

}
