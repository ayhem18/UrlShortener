package org.api.integrationTests.internals;

import org.api.internal.StubCompanyRepo;
import org.api.internal.StubCounterRepo;
import org.api.internal.StubUserRepo;
import org.data.repositories.CompanyRepository;
import org.data.repositories.CounterRepository;
import org.data.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StubDataConfig {

    // create beans for each stub repository
    @Bean
    public CounterRepository stubCounterRepository() {
        return new StubCounterRepo();
    }

    @Bean
    public CompanyRepository stubCompanyRepository() throws NoSuchFieldException, IllegalAccessException {
        return new StubCompanyRepo();
    }

    @Bean
    private StubCompanyRepo privateStubCompanyRepo() throws NoSuchFieldException, IllegalAccessException {
        return new StubCompanyRepo();
    }

    @Bean
    public UserRepository stubUserRepository(StubCompanyRepo scr) {
        return new StubUserRepo(scr);
    }
}
