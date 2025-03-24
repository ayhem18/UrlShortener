package org.urlService.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.access.Subscription;
import org.apache.commons.validator.routines.UrlValidator;
import org.apiUtils.commonClasses.TokenController;
import org.company.entities.CompanyUrlData;
import org.company.repositories.CompanyUrlDataRepository;
import org.company.entities.Company;
import org.company.entities.TopLevelDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.repositories.TokenUserLinkRepository;
import org.springframework.validation.annotation.Validated;
import org.user.entities.UrlEncoding;
import org.urlService.exceptions.UrlExceptions;
import org.url.UrlLevelEntity;
import org.url.UrlProcessor;
import org.company.repositories.TopLevelDomainRepository;
import org.user.entities.AppUser;
import org.user.repositories.UrlEncodingRepository;
import org.user.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Validated
@PropertySource("classpath:app.properties")
public class UrlController extends TokenController {

    private final CompanyUrlDataRepository urlDataRepo;
    private final UrlEncodingRepository urlEncodingRepo;
    private final TopLevelDomainRepository topLevelDomainRepo;
    private final UserRepository userRepository;
    private final TokenUserLinkRepository tokenUserLinkRepository;
    private final UrlProcessor urlProcessor;
    private final UrlValidator urlValidator;

    @Value("${server.port}")
    private int applicationPort;

    @Autowired
    public UrlController(CompanyUrlDataRepository urlDataRepo,
                         UrlEncodingRepository urlEncodingRepo,
                         TopLevelDomainRepository topLevelDomainRepo,
                         UserRepository userRepository, 
                         TokenUserLinkRepository tokenUserLinkRepository,
                         UrlProcessor urlProcessor) {
        super(userRepository, tokenUserLinkRepository);
        this.urlDataRepo = urlDataRepo;
        this.urlEncodingRepo = urlEncodingRepo;
        this.topLevelDomainRepo = topLevelDomainRepo;
        this.userRepository = userRepository;
        this.tokenUserLinkRepository = tokenUserLinkRepository;
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
                         TokenUserLinkRepository tokenUserLinkRepository,
                         UrlProcessor urlProcessor,
                         int port) {
        
        this(urlDataRepo, 
        urlEncodingRepo, 
        topLevelDomainRepo, 
        userRepository, 
        tokenUserLinkRepository, 
        urlProcessor);
        this.applicationPort = port;
    }


    //////////////////////////////////////// Methods for the encode/url endpoint ////////////////////////////////////////   
    public String getUrlEncodePrefix() {
        return "localhost:" + applicationPort + "/";
    }

    private Company verifyDailyLimit(AppUser user) {
        Company userCompany = user.getCompany();
        
        Subscription sub = userCompany.getSubscription();

        Integer userDailyLimit = sub.getEncodingDailyLimit();

        if (userDailyLimit != null) {
            LocalDateTime atStartOfDay = LocalDateTime.from(LocalDate.now().atStartOfDay());
            
            int todayCount = this.urlEncodingRepo.findByUserAndUrlEncodingTimeAfter(user, atStartOfDay).size();

            if (todayCount >= userDailyLimit) {
                throw new UrlExceptions.DailyLimitExceededException("The user's current subscription encoding daily limit is hit: " + userDailyLimit);
            }
        }

        return userCompany;
    }


    private Map.Entry<String, String> validateUrlCompanyConstraints(String url, Company userCompany) {
        // at this point, the user has not hit the daily limit

        // break down the url into path segments
        List<UrlLevelEntity> urlLevels = this.urlProcessor.breakdown(url);

        // check if the url matches the user's company top level domain
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
                    throw new UrlExceptions.UrlCompanyDomainExpired ("The url top level domain is deprecated");
                }

                else if (d.getDomainState() == TopLevelDomain.DomainState.INACTIVE) {
                    urlDomainPossibleWarning = "The top level domain is currently inactive. It might get deprecated in the future. The url was modified to use the active domain";                    
                }
                urlMatch = true;
            }

            if (d.getDomainState() == TopLevelDomain.DomainState.ACTIVE) {
                companyActivateLevelDomain = d.getDomain();
            }
        }

        if (!urlMatch) {
            throw new UrlExceptions.InvalidTopLevelDomainException("The Url does not match any of the user's company top level domains (active or inactive)");
        }

        // make sure to work only with the active top level domain
        urlLevels.set(1, new UrlLevelEntity(companyActivateLevelDomain, null, null, null));

        // build the url from the url levels 
        String urlString = this.urlProcessor.buildUrlFromUrlLevels(urlLevels);

        // create a map entry to return the url level entity and the warning
        return new AbstractMap.SimpleEntry<>(urlString, urlDomainPossibleWarning);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Transactional
    private String encodeUrlTransaction(Company userCompany, AppUser currentUser, Subscription sub, String urlWithActiveDomain) {
        CompanyUrlData companyUrlData = this.urlDataRepo.findByCompany(userCompany).get();

        List<Map<String, String>> encodedData = companyUrlData.getDataEncoded();
        List<Map<String, String>> decodedData = companyUrlData.getDataDecoded();

        String encodedUrl = this.urlProcessor.encode(urlWithActiveDomain, getUrlEncodePrefix(), companyUrlData.getCompanySiteHash(),
                encodedData, decodedData, sub.getMinParameterLength(), sub.getMinVariableLength());

        // make sure to update the urlEncodingRepo with the new encoded url
        this.urlEncodingRepo.save(new UrlEncoding(currentUser, urlWithActiveDomain, encodedUrl, currentUser.getUrlEncodingCount() + 1));
        // persist the changes to the url company data
        this.urlDataRepo.save(companyUrlData);
        return encodedUrl;
    }


    @GetMapping("/api/url/encode")
    public ResponseEntity<String> encodeUrl(@RequestParam String url, @AuthenticationPrincipal UserDetails currentUserDetails) throws JsonProcessingException {
        // make sure the user is authenticated.
        AppUser currentUser = this.validateUserToken(currentUserDetails);

        // 1. Validate the URL using Apache Commons Validator
        if (!urlValidator.isValid(url)) {
            throw new UrlExceptions.InvalidUrlException("Invalid URL");
        }

        Company userCompany = this.verifyDailyLimit(currentUser); 

        Subscription sub = userCompany.getSubscription();

        // 3. check whether the user is passing an url matching the user's company top level domain
        Map.Entry<String, String> urlLevelEntity = this.validateUrlCompanyConstraints(url, userCompany);

        String urlWithActiveDomain = urlLevelEntity.getKey();
        String urlDomainPossibleWarning = urlLevelEntity.getValue();

        // at this point, all constraints are met, time for the encoding transaction
        String encodedUrl = this.encodeUrlTransaction(userCompany, currentUser, sub, urlWithActiveDomain);
        
        // update the user's url encoding count
        currentUser.incrementUrlEncodingCount();
        this.userRepository.save(currentUser);


        HashMap<String, Object> map = new HashMap<>();
        map.put("encoded_url", encodedUrl);
        if (urlDomainPossibleWarning != null) {
            map.put("warning", urlDomainPossibleWarning);
        }

        return ResponseEntity.ok(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(map));
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