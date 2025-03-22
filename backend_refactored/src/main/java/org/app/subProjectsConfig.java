package org.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.utils.CustomGenerator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

// this class is created just to import the beans defined across all subprojects

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
        "org.authManagement.entities",
}
)
@PropertySource("classpath:mail.properties")
public class subProjectsConfig {
    // Inject properties from mail.properties
    @Value("${smtp.host:localhost}")
    private String smtpHost;
    
    @Value("${smtp.port:25}")
    private int smtpPort;
    
    @Value("${smtp.username:}")
    private String smtpUsername;
    
    @Value("${smtp.password:}")
    private String smtpPassword;
    
    @Value("${smtp.auth:false}")
    private String smtpAuth;
    
    @Value("${smtp.starttls.enable:false}")
    private String smtpStartTlsEnable;


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

    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }
}
