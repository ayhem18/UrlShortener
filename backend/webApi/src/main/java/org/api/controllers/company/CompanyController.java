package org.api.controllers.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.api.exceptions.CompanyExceptions;
import org.api.requests.CompanyRegisterRequest;
import org.common.RoleManager;
import org.common.Subscription;
import org.common.SubscriptionManager;
import org.data.entities.AppUser;
import org.data.entities.CollectionCounter;
import org.data.entities.Company;
import org.data.entities.CompanyWrapper;
import org.data.repositories.CompanyRepository;
import org.data.repositories.CounterRepository;
import org.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.utils.CustomGenerator;

import java.util.HashMap;
import java.util.Optional;


@RestController
@Validated
public class CompanyController {
    private static final int ROLE_TOKEN_LENGTH = 32;

    private final CompanyRepository companyRepo;
    private final CustomGenerator generator;
    private final UserRepository userRepo;
    private final CounterRepository counterRepo;

    @Autowired
    public CompanyController(CompanyRepository companyRepo,
                          UserRepository userRepo,
                          CounterRepository counterRepo,
                          CustomGenerator generator ) {
        this.companyRepo = companyRepo;
        this.generator = generator;
        this.counterRepo = counterRepo;
        this.userRepo = userRepo;
    }

    @Bean("companyControllerEncoder")
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter();
        return om;
    }


    private void validateNewCompany(CompanyRegisterRequest req) {
        // any new company must satisfy the uniqueness constraints:
        // unique id and unique site
        if (this.companyRepo.existsById(req.id())) {
            throw new CompanyExceptions.ExistingCompanyException("There is already a company with the given id.");
        }

        if (this.companyRepo.findBySite(req.site()).isPresent()) {
            throw new CompanyExceptions.ExistingSiteException("There is already a company with the given site");
        }
    }

    private long getCompanyCount() {
        String id = Company.COMPANY_COLLECTION_NAME;
        if (! this.counterRepo.existsById(id)) {
            CollectionCounter c = new CollectionCounter(id);
            this.counterRepo.save(c);
            // first object created
            c.setCount(1);
            return 0;
        }
        CollectionCounter c = this.counterRepo.findById(id).get();
        c.setCount(c.getCount() + 1);
        this.counterRepo.save(c);
        return c.getCount() - 1;
    }


    @PostMapping("api/auth/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody CompanyRegisterRequest req) throws JsonProcessingException {
        // check the uniqueness constraints
        validateNewCompany(req);
        // the new company is valid: get its creation order
        long companyOrder = this.getCompanyCount();

        // create the role tokens
        HashMap<String, String> roleTokens = new HashMap<>();

        // add the owner role token
        roleTokens.put(RoleManager.OWNER_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the admin role token
        roleTokens.put(RoleManager.ADMIN_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // add the registeredUser role token
        roleTokens.put(RoleManager.REGISTERED_USER_ROLE, this.generator.randomString(ROLE_TOKEN_LENGTH));

        // get the subscription from the SubscriptionManager
        Subscription sub = SubscriptionManager.getSubscription(req.subscription());

        // build the company object
        CompanyWrapper wrapper = new CompanyWrapper(req.id(), req.site(), sub, roleTokens, this.encoder(), this.generator, companyOrder);
        // make sure to call the serialize first, so that the "serializeSensitiveCount" field will be saved as "4"
        // in the database preventing the serialization of sensitive information beyond the very first time
        String companySerialized = wrapper.serialize(this.objectMapper());

        wrapper.save(this.companyRepo);

        return new ResponseEntity<>(companySerialized,
                HttpStatus.CREATED);
    }

    @DeleteMapping("api/company/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable String companyId,
                                                @AuthenticationPrincipal UserDetails currentUserDetails) throws RuntimeException{

        Optional<Company> company = this.companyRepo.findById(companyId);

        if (company.isEmpty()) {
            throw new CompanyExceptions.NoCompanyException("There is no company with the given Id");
        }

        // this function can be called by the Owner user of the company
        AppUser currentUser = this.userRepo.findById(currentUserDetails.getUsername()).get();


        if (!currentUser.getRole().toString().equals(RoleManager.OWNER_ROLE)) {
            throw new RuntimeException("Man you messed up the authentication");
        }

        // delete all users in the given company
        this.userRepo.deleteByCompany(company.get());

        // delete the company itself from the database
        this.companyRepo.deleteById(companyId);

        return new ResponseEntity<>("Company and users deleted successfully", HttpStatus.NO_CONTENT);
    }


//    @GetMapping("api/company/{companyId}/users")
//    public ResponseEntity<String> viewUsersInCompany(@PathVariable String companyId) throws JsonProcessingException {
//        Optional<Company> company = this.companyRepo.findById(companyId);
//
//        if (company.isEmpty()) {
//            throw new NoCompanyException("There is no company with the given Id");
//        }
//
//        List<AppUser> companyUsers = this.userRepo.findByCompany(company.get());
//
//        return new ResponseEntity<>(this.objectMapper().writeValueAsString(companyUsers), HttpStatus.OK);
//    }

    @GetMapping("api/company/{companyId}/details")
    public ResponseEntity<String> viewCompanyDetails(@PathVariable String companyId) throws JsonProcessingException {
        Optional<Company> company = this.companyRepo.findById(companyId);

        if (company.isEmpty()) {
            throw new CompanyExceptions.NoCompanyException("There is no company with the given Id");
        }

        return new ResponseEntity<>(this.objectMapper().writeValueAsString(company.get()), HttpStatus.OK);
    }
}
