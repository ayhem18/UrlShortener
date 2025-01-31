package org.api.controllers.url;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.api.exceptions.CompanyAndUserExceptions;
import org.api.exceptions.CompanyUniquenessConstraints;
import org.common.Subscription;
import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.data.entities.CompanyUrlData;
import org.data.entities.CompanyWrapper;
import org.data.repositories.CompanyUrlDataRepository;
import org.data.repositories.UserRepository;
import org.example.UrlDecoder;
import org.example.UrlEntity;
import org.example.UrlLevelEntity;
import org.example.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.utils.CustomGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Validated
@RestController
public class UrlController {

//    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;
    private final UrlDecoder urlDecoder;
    private final CompanyUrlDataRepository urlRepo;
    private final UrlValidator urlValidator;
    private final CustomGenerator customGenerator;

    @Autowired
    public UrlController(UserRepository userRepo,
                         CompanyUrlDataRepository urlRepo,
                         UrlDecoder urlDecoder,
                         UrlValidator urlValidator,
                         CustomGenerator gen
                         ) {
        this.userRepo = userRepo;
        this.urlDecoder = urlDecoder;
        this.urlRepo = urlRepo;
        this.urlValidator = urlValidator;
        this.customGenerator = gen;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter();
        return om;
    }

    private String validateUrl(String url) {
        this.urlValidator.validateUrl(url);

        if (url.startsWith("https://")) {
            String noHttpSubStr = url.substring("https://".length());
            if (! noHttpSubStr.startsWith("www.")) {
                url = "https://www." + url;
            }
        }
        else {
            String noHttpSubStr = url.substring("http://".length());
            if (! noHttpSubStr.startsWith("www.")) {
                url = "http://www." + url;
            }
        }

        return url;
    }


    private Company checkUrlSiteAlignment(List<UrlLevelEntity> levels, UserDetails userDetails) {

        // 4. make sure the site aligns with the user
        String userCompanySite = levels.getFirst().levelName();

        String username = userDetails.getUsername();
        Company userCompany = this.userRepo.findById(username).get().getCompany();
        CompanyWrapper companyWrapper = new CompanyWrapper(userCompany);

        if (! companyWrapper.getSite().equalsIgnoreCase(userCompanySite))  {
            throw new CompanyAndUserExceptions.
                    UserCompanyMisalignedException("The url site does not align with the user company site");
        }

        return userCompany;
    }

    private void checkSubscriptionLimits(List<UrlLevelEntity> levels, Company company, Subscription companySub)
            throws SubscriptionManager.SubscriptionViolatedException {
        // the first item is always the site and should not be considered in the limits validation
        Optional<CompanyUrlData> companyData = this.urlRepo.findByCompany(company);

        if (companyData.isEmpty()) {
            return;
        }

        // iterate through the url levels
        for (int i = 1; i < levels.size(); i++) {
            UrlLevelEntity entity = levels.get(i);

            // get the current at the level i
            for (UrlEntity valueType: UrlEntity.values()) {
                List<String> values = entity.get(valueType);

                HashMap<String, String> levelTypeData = companyData.get().getLevelTypeData(i, valueType).getKey();

                int count = levelTypeData.size();

                for (String val: values) {
                    count += (levelTypeData.containsKey(val)) ? 1 : 0;
                }

                if (count > companySub.get(valueType)) {
                    SubscriptionManager.throwSubscriptionViolatedException(valueType, i, count, companySub.get(valueType));
                }
            }
        }
    }

    private void addUrl(CompanyUrlData urlData, List<UrlLevelEntity> levels) {
        for (int i = 1; i < levels.size(); i++) {
            UrlLevelEntity entity = levels.get(i);

            // get the current at the level i
            for (UrlEntity valueType: UrlEntity.values()) {
                List<String> values = entity.get(valueType);

                for (String v: values) {
                    urlData.addValue(i, valueType, v, this.customGenerator);
                }
            }
        }
    }

    @GetMapping("api/url/encode/{url}")
    public ResponseEntity<String> encodeUrl(@PathVariable String url,
            @AuthenticationPrincipal UserDetails currentUserDetails)
                                            throws JsonProcessingException {
        //1. some preprocessing and verification
        url = validateUrl(url);

        // 2. break the url down into components
        List<UrlLevelEntity> levels = this.urlDecoder.breakdown(url);

        // 3. check the alignment between the site in the url and the user's company site
        Company userCompany = checkUrlSiteAlignment(levels, currentUserDetails);
        CompanyWrapper companyWrapper = new CompanyWrapper(userCompany);

        Subscription sub = companyWrapper.getSubscription();

        // 4. first check: does the number of levels in the url exceed the limit imposed by the subscription ?
        Integer maxNumLevels = sub.getMaxNumLevels();

        if ((maxNumLevels != null) && (levels.size() - 1 ) > sub.getMaxNumLevels()) {
            throw new SubscriptionManager.MaxNumLevelsSubExceeded(levels.size() - 1, maxNumLevels);
        }

        checkSubscriptionLimits(levels, userCompany, sub);

        Optional<CompanyUrlData> companyData = this.urlRepo.findByCompany(userCompany);

        CompanyUrlData urlData;

        if (companyData.isPresent()) {
            urlData = companyData.get();
        }
        else {
            String siteCompanyHash = this.customGenerator.generateId(this.urlRepo.count());
            urlData = new CompanyUrlData(userCompany, siteCompanyHash);
        }

        this.addUrl(urlData, levels);

        // save to the database
        this.urlRepo.save(urlData);

        //
        return new ResponseEntity<>(this.objectMapper().writeValueAsString(urlData), HttpStatus.OK);
    }

}
