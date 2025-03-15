package org.authManagement.integrationTests.configurations;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.utils.CustomGenerator;

import java.util.Properties;

// this class is created just to import the beans defined across all subprojects

@Configuration
@ComponentScan(basePackages = {
    "org.apiConfigurations",
    "org.authManagement.controllers",  
})

@EnableMongoRepositories(basePackages = {"org.company.repositories", 
                                        "org.user.repositories", 
                                        "org.tokens.repositories",
                                        "org.authManagement.repositories"
                                    }
)

@EntityScan(basePackages = {"org.company.entities", 
                    "org.user.entities", 
                    "org.tokens.entities", 
                    "org.authManagement.entities"
                }
)
// @EnableAutoConfiguration // this annotation is added to make Spring boot auto-configure the java MailSender Bean hopefully.
@PropertySource("classpath:mail.properties")
@SpringBootApplication
public class TestConfig {
    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }

    // not exactly sure how to make java spring boot auto-configure the java MailSender Bean.
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("${smtp.host}");
        mailSender.setPort(587);
        mailSender.setUsername("${smtp.username}");
        mailSender.setPassword("${smtp.password}");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "${smtp.auth}");
        props.put("mail.smtp.starttls.enable", "${smtp.starttls.enable}");
        
        return mailSender;
    }
}
