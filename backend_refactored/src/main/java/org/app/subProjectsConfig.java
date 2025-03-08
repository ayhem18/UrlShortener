package org.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.utils.CustomGenerator;

// this class is created just to import the beans defined across all subprojects

@Configuration
@ComponentScan(basePackages = {"org.appCore.repositories", "org.appCore.entities", "org.appCore.controllers", "org.appCore.configurations"})
@EnableMongoRepositories(basePackages = {"org.appCore.repositories"})
@EntityScan(basePackages = {"org.appCore.entities"})
public class subProjectsConfig {
    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }

    
}
