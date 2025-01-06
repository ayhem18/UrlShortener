package com.url_shortener.Urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.DigestException;
import java.util.HashMap;
import java.util.Map;


@Validated
@RestController
public class UrlController {

    private final UrlRepository urlRepo;
    private final UrlValidator urlValidator;


    // since the UrlValidator class is annotated with @Configuration
    // we can assign a reference as a field of this class by using the @Autowired annotation
    // basically constructor dependency injection
    @Autowired
    public UrlController(UrlRepository urlRepo, UrlValidator urlValidator) {
        this.urlRepo = urlRepo;
        this.urlValidator = urlValidator;
    }

//    @Bean(name = "urlEncoder")
//    public TextEncryptor urlEncoder() {
//        return  new RsaRawEncryptor();
//    }


    @PostMapping("api/url/encode")
    public String shortenUrl(@Valid @RequestBody UrlReq urlReq) throws JsonProcessingException, DigestException {
        String urlStr = urlReq.getUrl();
        // verify the passed string is indeed an url
        boolean urlValid = this.urlValidator.validate(urlStr);

        if (!urlValid) {
            // this exception will be intercepted by the UrlExceptionHandler
            throw  new InvalidUrlException("The passed url format is incorrect");
        }

//        String urlHashed;
//
//        // hash the url
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            md.update(urlStr.getBytes());
//
//            urlHashed = new String(((MessageDigest) md.clone()).digest());
//
//        } catch (CloneNotSupportedException | NoSuchAlgorithmException cnse) {
//            throw new DigestException("couldn't make digest of partial content");
//        }

        Url url = new Url(urlStr, String.valueOf(urlStr.hashCode()));

        // save the url and its hash to the database
        this.urlRepo.save(url);

        // return both the url and its has
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(url);
    }

    @GetMapping("api/url/url_count")
    public String urlCount() throws JsonProcessingException {
        Map<String, Long> result = new HashMap<>();
        result.put("count", this.urlRepo.count());
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    @GetMapping("api/url/redirect/{hash}")
    public String redirect(@PathVariable(value="hash") String hashUrl) throws NoHashedUrlException, JsonProcessingException{
        Url url = this.urlRepo.findByHash(hashUrl).
                orElseThrow(() -> new NoHashedUrlException("The passed hashed url is not saved in the database"));

        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(url);
    }
}
