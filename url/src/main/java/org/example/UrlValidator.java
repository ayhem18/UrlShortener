package org.example;

import java.util.regex.Pattern;


public class UrlValidator {
    private final Pattern noSpacePattern = Pattern.compile(
            "http(s)?://(www\\.)*[a-zA-Z0-9]+\\.[a-zA-Z]{3,}(/+[a-zA-Z0-9?=_!.-]+)*"
    );

    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message) {
            super(message);
        }
    }

    public void validateUrl(String url) {
        if (!noSpacePattern.matcher(url).matches()) {
            throw new InvalidUrlException("The passed url is invalid");
        }
    }
}