package com.url_shortener.User;

import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;



@Configuration
class UrlValidator {
    private final Pattern noSpacePattern = Pattern.compile(
            "http(s)?://(www\\.)*[a-zA-Z0-9]+\\.[a-zA-Z]{3,}(/+[a-zA-Z0-9?=_!.-]+)*"
    );

    public boolean validate(String url) {
        return noSpacePattern.matcher(url).matches();
    }

}