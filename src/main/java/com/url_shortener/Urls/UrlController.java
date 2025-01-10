package com.url_shortener.Urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


//@Validated
//@RestController
//public class UrlController {
//
//    private final UrlRepository urlRepo;
//    private final UrlValidator urlValidator;
//
//
//    // since the UrlValidator class is annotated with @Configuration
//    // we can assign a reference as a field of this class by using the @Autowired annotation
//    // basically constructor dependency injection
//    @Autowired
//    public UrlController(UrlRepository urlRepo, UrlValidator urlValidator) {
//        this.urlRepo = urlRepo;
//        this.urlValidator = urlValidator;
//    }
//
//
//    @PostMapping("api/url/encode")
//    public ResponseEntity<String> shortenUrl(@Valid @RequestBody UrlReq urlReq) throws JsonProcessingException {
//        String urlStr = urlReq.getUrl();
//        // verify the passed string is indeed an url
//        boolean urlValid = this.urlValidator.validate(urlStr);
//
//        if (!urlValid) {
//            // this exception will be intercepted by the UrlExceptionHandler
//            throw  new InvalidUrlException("The passed url format is incorrect");
//        }
//
//
//        if (this.urlRepo.existsById(urlStr)) {
//            return new ResponseEntity<>(
//                    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this.urlRepo.findById(urlStr).get()), // body
//                    HttpStatus.OK // status code 200 since the object wasn't created.
//            );
//        }
//
//        Url url = new Url(urlStr, String.valueOf(urlStr.hashCode()));
//        // save the url and its hash to the database
//        this.urlRepo.save(url);
//
//        // if I return only the String, the status code would be 200
//        // return a Response entity specifying the status code myself (probably this has already been solved by someone and can be avoided)
//
//        // according to the HTTPS specification, a successful post request should return a Response with the
//        // code 201 and a header with the location of the newly-allocated resource
//        // this can be done as follows: ResponseEntity.created(Uri).build()
//
//        // check  the URI
//
//        return new ResponseEntity<>(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(url),
//                HttpStatus.CREATED);
//    }
//
//    @GetMapping("api/url/url_count")
//    public String urlCount() throws JsonProcessingException {
//        Map<String, Long> result = new HashMap<>();
//        result.put("count", this.urlRepo.count());
//        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
//    }
//
//    @GetMapping("api/url/redirect/{hash}")
//    public String redirect(@PathVariable(value="hash") String hashUrl) throws JsonProcessingException, NoHashedUrlException{
//        // need to learn about redirects to properly complete this method
//        Url url = this.urlRepo.findByHash(hashUrl).
//                orElseThrow(() -> new NoHashedUrlException("The passed hashed url is not saved in the database"));
//
//        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(url);
//    }
//}
