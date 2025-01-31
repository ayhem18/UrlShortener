package org.api.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.api.integrationTests.internals.StubControllerConfig;
import org.example.UrlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

public class DependenciesTest {
    private ApplicationContext context;

    @BeforeEach
    void setContext() {
        context = SpringApplication.run(StubControllerConfig.class);
    }

    @Test
    void testUtilBeans() {
        // extract the utils beans:
        List<Class> classes = List.of(PasswordEncoder.class, ObjectMapper.class, UrlValidator.class);

        for (Class c: classes) {
            Assertions.assertDoesNotThrow( () -> context.getBean(c));
        }
    }

}
