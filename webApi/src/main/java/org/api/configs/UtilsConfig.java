package org.api.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.UrlValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.utils.CustomGenerator;

@Configuration
public class UtilsConfig {

    @Bean
    @Primary()
    // this annotation sets the bean created by this function as the default one when invoked by type
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.writerWithDefaultPrettyPrinter();
        return om;
    }

    @Bean
    public UrlValidator urlValidator() {
        return new UrlValidator();
    }

    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }
}