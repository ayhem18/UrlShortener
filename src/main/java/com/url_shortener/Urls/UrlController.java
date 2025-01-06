package com.url_shortener.Urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class UrlController {
    @PostMapping("api/url/encode")
    public String shortenUrl(@Valid @RequestBody UrlRequest urlRequest) throws JsonProcessingException {
        return "thank you for sending a request";
    }
}
