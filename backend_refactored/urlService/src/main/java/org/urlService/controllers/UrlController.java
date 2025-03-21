package org.urlService.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.access.Subscription;
import org.apache.commons.validator.routines.UrlValidator;
import org.authManagement.entities.CompanyUrlData;
import org.authManagement.repositories.CompanyUrlDataRepository;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.urlService.exceptions.UrlExceptions;
import org.urlService.repositories.UrlEncodingRepository;
import org.url.UrlLevelEntity;
import org.url.UrlProcessor;
import org.company.repositories.TopLevelDomainRepository;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@PropertySource("classpath:app.properties")
public class UrlController {

    private final CompanyUrlDataRepository urlDataRepo;
    private final UrlEncodingRepository urlEncodingRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final UserRepository userRepository;
    private final UrlProcessor urlProcessor;
    private final UrlValidator urlValidator;

    @Value("${server.port}")
    private int applicationPort;

    @Autowired
    public UrlController(CompanyUrlDataRepository urlDataRepo,
                        UrlEncodingRepository urlEncodingRepo, 
                        TopLevelDomainRepository topLevelDomainRepo, 
                        UserRepository userRepository,
                        UrlProcessor urlProcessor) {
        this.urlDataRepo = urlDataRepo;
        this.urlEncodingRepo = urlEncodingRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.userRepository = userRepository;
        this.urlProcessor = urlProcessor;
        
        // limit the protocols to http and https and allow local urls
        String[] schemes = {"http", "https"};
        this.urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
    }

    // added for unit testing without loading external resources
    public UrlController(CompanyUrlDataRepository urlDataRepo,
                        UrlEncodingRepository urlEncodingRepo, 
                        TopLevelDomainRepository topLevelDomainRepo, 
                        UserRepository userRepository,
                        UrlProcessor urlProcessor,
                        int port) {
        this(urlDataRepo, urlEncodingRepo, topLevelDomainRepo, userRepository, urlProcessor);
        this.applicationPort = port;
    }


    @PostMapping("/api/url/encode/{url}")
    @SuppressWarnings({"OptionalGetWithoutIsPresent"})
    public ResponseEntity<String> encodeUrl(@PathVariable String url, @AuthenticationPrincipal UserDetails currentUserDetails) throws JsonProcessingException {
        // 1. Validate the URL using Apache Commons Validator
        if (!urlValidator.isValid(url)) {
            throw new UrlExceptions.InvalidUrlException("Invalid URL");
        }

        // the fact that user is authenticated guarantees that the user exists (get does not return null)
        AppUser currentUser = userRepository.findByUsername(currentUserDetails.getUsername()).get();
        
        // 2. extract the user information 
        Company userCompany = currentUser.getCompany();
        
        Subscription sub = userCompany.getSubscription();

        Integer userDailyLimit = sub.getEncodingDailyLimit();

        if (userDailyLimit != null) {
            LocalDateTime atStartOfDay = LocalDateTime.from(LocalDate.now().atStartOfDay());
            
            int todayCount = this.urlEncodingRepo.findByUserAndUrlEncodingTimeAfter(currentUser, atStartOfDay).size(); 

            if (todayCount >= userDailyLimit) {
                throw new UrlExceptions.DailyLimitExceededException("The user's current subscription encoding daily limit is hit: " + userDailyLimit);
            }
        }

        // at this point, the user has not hit the daily limit

        // 3. check whether the user is passing a url matching the user's company top level domain 

        List<UrlLevelEntity> urlLevels = this.urlProcessor.breakdown(url);

        String urlTopLevelDomain = urlLevels.get(1).levelName();

        if (!urlTopLevelDomain.startsWith("www.")) {
            urlTopLevelDomain = "www." + urlTopLevelDomain;
        }


        List<TopLevelDomain> companyDomains = this.topLevelDomainRepo.findByCompany(userCompany);

        // to save a warning in case the url matches an inactive top level domain.
        String urlDomainPossibleWarning = null;

        boolean urlMatch = false;

        String companyActivateLevelDomain = null;
        for (TopLevelDomain d : companyDomains) {
            
            if (d.getDomain().equals(urlTopLevelDomain)) {
                // check the state of the topLevelDomain
                // if it is deprecated: raise an error
                if (d.getDomainState() == TopLevelDomain.DomainState.DEPRECATED) {
                    throw new UrlExceptions.UrlCompanyDomainExpired ("The URL does not match the user's company top level domain");
                }

                else if (d.getDomainState() == TopLevelDomain.DomainState.INACTIVE) {
                    urlDomainPossibleWarning = "The top level domain is currently inactive. It might get deprecated in the future. The url was modified to use the activte domain";                    
                }
                urlMatch = true;
            }

            if (d.getDomainState() == TopLevelDomain.DomainState.ACTIVE) {
                companyActivateLevelDomain = d.getDomain();
            }
        }

        urlLevels.set(1, new UrlLevelEntity(companyActivateLevelDomain, null, null, null));

        if (!urlMatch) {
            throw new UrlExceptions.InvalidTopLevelDomainException("The Url does not match any of the user's company top level domains (active or inactive)");
        }
        
        // at this point, the url is valid and matches the user's company top level domain 
        // time to encode: extract the company url data from the repo
        CompanyUrlData companyUrlData = this.urlDataRepo.findByCompany(userCompany).get();

        List<Map<String, String>> encodedData = companyUrlData.getDataEncoded();
        List<Map<String, String>> decodedData = companyUrlData.getDataDecoded();

        String prefix = "localhost:" + applicationPort + "/";

        String encodedUrl = this.urlProcessor.encode(url, prefix, companyUrlData.getCompanySiteHash(),
                encodedData, decodedData, sub.getMinParameterLength(), sub.getMinVariableLength());

        String res = null;
        if (urlDomainPossibleWarning != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("encoded_url", encodedUrl);
            map.put("warning", urlDomainPossibleWarning);

            res = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(map);
        }
        else {
            res = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(encodedUrl);
        }

        return ResponseEntity.ok(res);
    }



    @PostMapping("/api/url/decode/{url}")
    public ResponseEntity<String> decodeUrl(@PathVariable String url, @AuthenticationPrincipal AppUser user) {
        //
        return null;
    }


    @GetMapping("/api/url/history")
    public ResponseEntity<String> getHistory(@AuthenticationPrincipal AppUser user) {
        //
        return null;
    }

}