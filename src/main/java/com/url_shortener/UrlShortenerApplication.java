package com.url_shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



class Person {
	String FirstName;
	String lastName;

	public Person() {

	}

	public Person(String firstName, String lastName) {
		FirstName = firstName;
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "Person{" +
				"FirstName='" + FirstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}
}


class Car {
	Person owner;

	public Car(Person owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "Car{" +
				"owner=" + owner +
				'}';
	}
}


@Configuration
class someConfiguration {
	@Bean
	Person personBean(){
		return new Person("Ayhem", "Bouabid");
	}

	@Bean
	Car carBean(Person person) {
//		Person person = new Person("p1", "p2");
		return new Car(person);
	}
}

// the idea here is to


// this is application starting point
@SpringBootApplication
public class UrlShortenerApplication {

	public static void main(String[] args) {
		// the application context is basically
		ApplicationContext context = SpringApplication.run(UrlShortenerApplication.class, args);
		System.out.println("\n" + context.getBean(Person.class) + "\n");
		System.out.println("\n" + context.getBean(Car.class) + "\n");

	}
}


