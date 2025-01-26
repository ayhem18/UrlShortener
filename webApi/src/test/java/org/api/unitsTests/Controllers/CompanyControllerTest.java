package org.api.unitsTests.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.api.controllers.company.CompanyController;
import org.api.exceptions.ExistingCompanyException;
import org.api.internal.StubCompanyRepo;
import org.api.internal.StubUserRepo;
import org.api.requests.CompanyRegisterRequest;
import org.common.SubscriptionManager;
import org.data.entities.Company;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.utils.CustomGenerator;

public class CompanyControllerTest {

    private final StubCompanyRepo cRepo;
    private final StubUserRepo uRepo;
    private final CustomGenerator gen = new CustomGenerator();
    private final CompanyController comCon;

    // use mockito to fake the random generation of Tokens
    public CompanyControllerTest() {
        this.cRepo = new StubCompanyRepo();
        this.uRepo = new StubUserRepo(cRepo);
        this.comCon = new CompanyController(this.cRepo, this.uRepo, this.gen);
    }

    @Test
    void testRegisterErrorExistingCompany() {
        for (Company c: this.cRepo.getDb()) {
            // create a register request based on the given company
            CompanyRegisterRequest req = new CompanyRegisterRequest(c.getId(),
                    c.getSite(),
                    c.getSubscription().getTier());

            Assertions.assertThrows(ExistingCompanyException.class, () -> this.comCon.registerCompany(req));
        }
    }
}
