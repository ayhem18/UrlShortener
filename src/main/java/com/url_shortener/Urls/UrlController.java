package com.url_shortener.Urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.url_shortener.Service.Company.Company;
import com.url_shortener.Service.User.UserRepository;
import com.url_shortener.Service.UserCompanyMisalignedException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Validated
@RestController
public class UrlController {

//    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final UrlDecoder urlDecoder;
    // since the UrlValidator class is annotated with @Configuration
    // we can assign a reference as a field of this class by using the @Autowired annotation
    // basically constructor dependency injection
    @Autowired
    public UrlController(UserRepository userRepo, UrlDecoder urlDecoder) {
//        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.urlDecoder = urlDecoder;
    }

    private Map.Entry<String, Company> checkUrlSiteAlignment(UrlReq req, UserDetails userDetails) {
        // 1. extract the url
        String urlStr = req.getUrl();

        // 2. since the url might not start with "www.", add it to the url string
        if (! urlStr.startsWith("www.")) {
            urlStr = "www." + urlStr;
        }

        // 3. break the url down into components
        List<UrlLevelEntity> levels = this.urlDecoder.breakdown(urlStr);

        // 4. make sure the site aligns with the user
        String userCompanySite = levels.getFirst().levelName();

        String username = userDetails.getUsername();
        Company userCompany = this.userRepo.findById(username).get().getCompany();

        if (! userCompany.getSite().equalsIgnoreCase(userCompanySite))  {
            throw new UserCompanyMisalignedException("The url site does not align with the user company site");
        }

        return Map.entry(urlStr, userCompany);
    }




    @PostMapping
    public ResponseEntity<String> encodeUrl(@Valid @RequestBody UrlReq req,
                                            @AuthenticationPrincipal UserDetails currentUserDetails)
                                            throws JsonProcessingException {
        // check the alignment between the site in the url and the user's company site

        Map.Entry<String, Company> pair = checkUrlSiteAlignment(req, currentUserDetails);
        String urlStr = pair.getKey();
        Company userCompany = pair.getValue();

        Subscription sub = userCompany.getSubscription();

        // decode the url
        List<UrlLevelEntity> levels = this.urlDecoder.breakdown(urlStr);

        // first check
        Integer maxNumLevels = sub.getMaxNumLevels();

        if ((maxNumLevels != null) && (levels.size() - 1 ) > sub.getMaxNumLevels()) {
            throw new MaxNumLevelsSubExceeded(levels.size() - 1, maxNumLevels);
        }

        //
        return ResponseEntity.ok().build();
    }

}
