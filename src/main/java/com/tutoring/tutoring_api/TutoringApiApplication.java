package com.tutoring.tutoring_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TutoringApiApplication {

	public static void main(String[] args) {
		System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5433/tutoring_db");
		System.setProperty("spring.datasource.username", "postgres");
		System.setProperty("spring.datasource.password", "0527");
		System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
		SpringApplication.run(TutoringApiApplication.class, args);
	}

}