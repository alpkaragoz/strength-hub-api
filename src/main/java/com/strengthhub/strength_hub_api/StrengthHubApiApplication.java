package com.strengthhub.strength_hub_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StrengthHubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StrengthHubApiApplication.class, args);
	}

}
