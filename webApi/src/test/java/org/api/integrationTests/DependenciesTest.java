package org.api.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.api.integrationTests.internals.StubControllerConfig;
import org.api.internal.StubCompanyRepo;
import org.api.internal.StubCounterRepo;
import org.api.internal.StubUserRepo;
import org.data.repositories.CompanyRepository;
import org.data.repositories.CounterRepository;
import org.data.repositories.UserRepository;
import org.example.UrlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.utils.CustomGenerator;

import java.util.List;

public class DependenciesTest {
    private ApplicationContext context;

    // according to the link below:
    // https://www.baeldung.com/spring-boot-application-context-exception
    // to pass the tests, the application needs to be configured as a non-web configuration
    @BeforeEach
    void setContext() {
        context = SpringApplication.run(StubControllerConfig.class);
    }

    @Test
    void testUtilBeans() {
        // extract the utils beans:
        List<Class> UtilClasses = List.of(PasswordEncoder.class, ObjectMapper.class,
                UrlValidator.class,
                CustomGenerator.class);

        for (Class c: UtilClasses) {
            Assertions.assertDoesNotThrow( () -> context.getBean(c));
        }
    }

    @Test
    void testDataBeans() {
        List<Class> dataClasses = List.of(CompanyRepository.class,
                UserRepository.class,
                CounterRepository.class);


        List<Class> actualClasses = List.of(StubCompanyRepo.class,
                StubUserRepo.class,
                StubCounterRepo.class);

        // a test just to stop execution in case the objects above were modified
        Assertions.assertEquals(actualClasses.size(), dataClasses.size());

        for (int i = 0; i < actualClasses.size(); i++) {
            int finalI = i;
            Assertions.assertDoesNotThrow( () -> context.getBean(dataClasses.get(finalI)));
            Assertions.assertInstanceOf(actualClasses.get(i), context.getBean(dataClasses.get(finalI)));
        }
    }
}
