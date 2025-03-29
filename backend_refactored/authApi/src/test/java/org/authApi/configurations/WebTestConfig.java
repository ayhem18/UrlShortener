package org.authApi.configurations;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.utils.CustomGenerator;

@Configuration
@EntityScan(basePackages = {"org.company.entities",
        "org.user.entities",
        "org.tokens.entities",
        "org.apiUtils.entities"
}
)
@ComponentScan(basePackages = {
        "org.apiUtils",
        "org.authManagement.controllers",
        "org.stubs.repositories"
})
@PropertySource("classpath:mail.properties")
public class WebTestConfig {

    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}

