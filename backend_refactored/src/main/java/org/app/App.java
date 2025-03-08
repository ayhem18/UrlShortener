package org.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"org.app", "org.appCore", "org.access"})
@EnableMongoRepositories(basePackages = {"org.appCore.repositories"})
@EntityScan(basePackages = {"org.appCore.entities"})
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
