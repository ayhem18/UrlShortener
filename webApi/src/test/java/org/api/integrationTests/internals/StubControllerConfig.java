package org.api.integrationTests.internals;




import org.api.configs.UtilsConfig;
import org.api.controllers.company.CompanyController;
import org.data.repositories.CompanyRepository;
import org.data.repositories.CounterRepository;
import org.data.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.utils.CustomGenerator;


// controllers depend on objects from Url, utils and common subprojects. Such beans should be
// configured in the UtilsConfig class (or configuration file if you may)
// Controllers also depend on repositories: for testing: they are configured in the StubDataConfig
// both configurations should be imported:

@Configuration
@Import({UtilsConfig.class, StubDataConfig.class})
public class StubControllerConfig {
    // create the controller beans
    @Bean
    public CompanyController companyController(CompanyRepository companyRepository,
                                               UserRepository userRepository,
                                               CounterRepository counterRepository,
                                               CustomGenerator customGenerator) {
        return new CompanyController(companyRepository, userRepository, counterRepository, customGenerator);
    }
}
