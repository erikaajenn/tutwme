package com.tutoring.tutoring_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TutoringApiApplication {

	public static void main(String[] args) {
		boolean isRailway = System.getenv("RAILWAY_ENVIRONMENT") != null;

		if (isRailway) {
			System.setProperty("spring.datasource.url", "jdbc:postgresql://hopper.proxy.rlwy.net:50797/railway");
			System.setProperty("spring.datasource.username", "postgres");
			System.setProperty("spring.datasource.password", "eTeGKacXhppCeFfvOKRJaYnLbfmYWOir");
		} else {
			System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5433/tutoring_db");
			System.setProperty("spring.datasource.username", "postgres");
			System.setProperty("spring.datasource.password", "0527");
		}

		System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
		System.setProperty("spring.sql.init.mode", "never");
		SpringApplication.run(TutoringApiApplication.class, args);
	}
}