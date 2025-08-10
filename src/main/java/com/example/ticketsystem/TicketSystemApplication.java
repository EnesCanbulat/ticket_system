package com.example.ticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class TicketSystemApplication {

	public static void main(String[] args) {
		System.out.println(" Starting Ticket Management System...");
		System.out.println(" JDK Version: " + System.getProperty("java.version"));
		System.out.println(" Spring Boot Version: 3.5.3");

		var context = SpringApplication.run(TicketSystemApplication.class, args);

		System.out.println(" Ticket Management System started successfully!");
		System.out.println(" Application is running on: http://localhost:8080");
		System.out.println(" Health Check: http://localhost:8080/api/utility/health");
		System.out.println(" Actuator: http://localhost:8080/actuator/health");
	}
}