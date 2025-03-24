package org.urlService.configurations;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.url.UrlProcessor;
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
        "org.stubs.repositories",
        "org.urlService.controllers",
})
@PropertySource("classpath:mail.properties")
@SuppressWarnings("unused")
public class UrlServiceWebConfig {

    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean // this should inject the customGenerator bean into the urlProcessor bean
    public UrlProcessor urlProcessor(CustomGenerator customGenerator) {
        return new UrlProcessor(customGenerator);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // this is used solely for testing purposes
    }

}

