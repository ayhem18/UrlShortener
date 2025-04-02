package org.apiUtils.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI myOpenAPI() {
                Server devServer = new Server();
                devServer.setUrl("http://localhost:8018");
                devServer.setDescription("Server URL in Development environment");

                Contact contact = new Contact();
                contact.setEmail("contact@yourcompany.com");
                contact.setName("API Support");
                // contact.setUrl("https://www.yourcompany.com");

                License mitLicense = new License()
                        .name("MIT License")
                        .url("https://choosealicense.com/licenses/mit/");

                Info info = new Info()
                        .title("Company Management API")
                        .version("1.0")
                        .contact(contact)
                        .description("This API exposes endpoints to manage companies, users, and authentication.")
                        .license(mitLicense);

                return new OpenAPI()
                        .info(info)
                        .servers(List.of(devServer));
        }
} 