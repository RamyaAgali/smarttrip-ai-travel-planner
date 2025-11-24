package com.smarttrip.tripservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smarttrip.tripservice")
@EnableJpaRepositories(basePackages = "com.smarttrip.tripservice.repository")
@EntityScan(basePackages = "com.smarttrip.tripservice.model")
@EnableScheduling
public class TripserviceApplication{

	public static void main(String[] args) {
		SpringApplication.run(TripserviceApplication.class, args);
	}

}
