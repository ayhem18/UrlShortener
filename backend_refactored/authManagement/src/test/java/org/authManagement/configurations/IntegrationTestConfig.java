package org.authManagement.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.utils.CustomGenerator;

import java.util.Properties;

@SpringBootApplication
@ComponentScan(basePackages = {
    "org.apiUtils",
    "org.authManagement.controllers",  
})
@EnableMongoRepositories(basePackages = {"org.company.repositories", 
                                        "org.user.repositories", 
                                        "org.tokens.repositories",
                                        "org.apiUtils.repositories"
                                    }
)
@EntityScan(basePackages = {"org.company.entities",
        "org.user.entities",
        "org.tokens.entities",
        "org.apiUtils.entities"
})
@PropertySource("classpath:mail.properties")
public class IntegrationTestConfig {
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
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(smtpUsername);
        mailSender.setPassword(smtpPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
        
        return mailSender;
    }

    // create some beans needed for the app
    @Bean
    public CustomGenerator customGenerator() {
        return new CustomGenerator();
    }
}
