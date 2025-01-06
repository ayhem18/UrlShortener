package com.url_shortener.Urls;

import org.springframework.context.annotation.Configuration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}

class NoHashedUrlException extends  RuntimeException {
    public NoHashedUrlException(String message) {
        super(message);
    }
}

@Configuration
class UrlValidator {
    private final Pattern noSpacePattern = Pattern.compile("https://www\\.[a-zA-Z0-9$_?=/-]+\\.[a-zA-Z]{3,}");

    public boolean validate(String url) {
        return noSpacePattern.matcher(url).matches();
    }

}