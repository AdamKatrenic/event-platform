package com.adam.event_platform;

import com.adam.event_platform.dto.BookingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
class EventPlatformApplicationTests {

	@MockBean
	private KafkaTemplate<String, BookingEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}