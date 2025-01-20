package org.api.exceptions;

public class NoCompanyException extends RuntimeException {
    public NoCompanyException(String message) {
        super(message);
    }
}
