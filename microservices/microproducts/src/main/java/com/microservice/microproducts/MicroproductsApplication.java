package com.microservice.microproducts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication(scanBasePackages = {"com.microservice.microproducts", "com.microservice.Helper"})
@EnableDiscoveryClient 
public class MicroproductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroproductsApplication.class, args);
	}

}
