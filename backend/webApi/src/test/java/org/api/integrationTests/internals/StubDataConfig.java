package org.api.integrationTests.internals;

import org.api.internal.StubCompanyRepo;
import org.api.internal.StubCounterRepo;
import org.api.internal.StubUserRepo;
import org.data.repositories.CompanyRepository;
import org.data.repositories.CounterRepository;
import org.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StubDataConfig {

    // create beans for each stub repository
    @Bean
    public CounterRepository stubCounterRepository() {
        return new StubCounterRepo();
    }

    @Bean
    @Primary // adding this annotation discards the private stub repository bean declared below
    public CompanyRepository stubCompanyRepository() throws NoSuchFieldException, IllegalAccessException {
        return new StubCompanyRepo();
    }

    // beans cannot be private !!!
    @Bean
    @Qualifier("privateStubCompanyRepo")
    public StubCompanyRepo privateStubCompanyRepo() throws NoSuchFieldException, IllegalAccessException {
        return new StubCompanyRepo();
    }

    @Bean
    public UserRepository stubUserRepository(StubCompanyRepo scr) {
        return new StubUserRepo(scr);
    }
}
