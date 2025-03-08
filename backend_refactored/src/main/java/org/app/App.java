package org.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(subProjectsConfig.class) // import the beans defined across all subprojects
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
 