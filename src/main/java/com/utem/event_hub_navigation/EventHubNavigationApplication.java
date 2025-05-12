package com.utem.event_hub_navigation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.utem.event_hub_navigation")
public class EventHubNavigationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventHubNavigationApplication.class, args);
	}

}
