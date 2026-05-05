package com.microservice.microspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MicrospringApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrospringApplication.class, args);
	}

}
