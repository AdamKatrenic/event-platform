package com.adam.event_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class})
@EnableCaching
public class EventPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventPlatformApplication.class, args);
	}

}
