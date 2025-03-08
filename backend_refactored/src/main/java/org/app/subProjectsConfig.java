package org.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// this class is created just to import the beans defined across all sub-projects

@Configuration
@ComponentScan(basePackages = {"org.appCore.repositories", "org.appCore.entities", "org.appCore.controllers", "org.appCore.configurations"})
public class subProjectsConfig {
    
}
