package org.api.exceptions;

public class ExistingCompanyException extends RuntimeException {
    public ExistingCompanyException(String message) {
        super(message);
    }
}
