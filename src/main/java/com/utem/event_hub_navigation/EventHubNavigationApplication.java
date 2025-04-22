package com.utem.event_hub_navigation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing

@SpringBootApplication
public class EventHubNavigationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventHubNavigationApplication.class, args);
	}

}
