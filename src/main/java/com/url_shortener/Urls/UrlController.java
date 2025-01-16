package com.url_shortener.Urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.url_shortener.Service.Company.Company;
import com.url_shortener.Service.User.UserRepository;
import com.url_shortener.Service.UserCompanyMisalignedException;
import com.url_shortener.Urls.UrlData.CompanyUrlData;
import com.url_shortener.Urls.UrlData.CompanyUrlDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Validated
@RestController
public class UrlController {

//    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final UrlDecoder urlDecoder;
    private final CompanyUrlDataRepository urlRepo;

    @Autowired
    public UrlController(UserRepository userRepo,
                         CompanyUrlDataRepository urlRepo,
                         UrlDecoder urlDecoder) {
//        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
        this.urlDecoder = urlDecoder;
        this.urlRepo = urlRepo;
    }

    private Company checkUrlSiteAlignment(List<UrlLevelEntity> levels, UserDetails userDetails) {

        // 4. make sure the site aligns with the user
        String userCompanySite = levels.getFirst().levelName();

        String username = userDetails.getUsername();
        Company userCompany = this.userRepo.findById(username).get().getCompany();

        if (! userCompany.getSite().equalsIgnoreCase(userCompanySite))  {
            throw new UserCompanyMisalignedException("The url site does not align with the user company site");
        }

        return userCompany;
    }

    private boolean checkSubscriptionLimits(List<UrlLevelEntity> levels, Company company, Subscription companySub)
            throws SubscriptionViolatedException {
        // the first item is always the site and should not be considered in the limits validation
        Optional<CompanyUrlData> companyData = this.urlRepo.findByCompany(company);

        if (companyData.isEmpty()) {
            return true;
        }

        for (int i = 1; i < levels.size(); i++) {
            UrlLevelEntity entity = levels.get(i);

            // get the current at the level i
            for (String valueType: CompanyUrlData.VALUE_TYPES) {
                List<String> values = entity.get(valueType);

                HashMap<String, String> levelTypeData = companyData.get().getLevelTypeData(i, valueType).getKey();

                int count = levelTypeData.size();

                for (String val: values) {
                    if (val == null) {
                        continue;
                    }

                    if (! levelTypeData.containsKey(val.toLowerCase())) {
                        count += 1;
                    }
                }

            }
        }


        return false;
    }


    @GetMapping("api/url/{url}")
    public ResponseEntity<String> encodeUrl(@PathVariable String url,
            @AuthenticationPrincipal UserDetails currentUserDetails)
                                            throws JsonProcessingException {
        // 1. since the url might not start with "www.", add it to the url string
        if (! url.startsWith("www.")) {
            url = "www." + url;
        }

        // 3. break the url down into components
        List<UrlLevelEntity> levels = this.urlDecoder.breakdown(url);

        // check the alignment between the site in the url and the user's company site

        Company userCompany = checkUrlSiteAlignment(levels, currentUserDetails);

        Subscription sub = userCompany.getSubscription();

        // first check
        Integer maxNumLevels = sub.getMaxNumLevels();

        if ((maxNumLevels != null) && (levels.size() - 1 ) > sub.getMaxNumLevels()) {
            throw new MaxNumLevelsSubExceeded(levels.size() - 1, maxNumLevels);
        }

        //
        return ResponseEntity.ok().build();
    }

}
