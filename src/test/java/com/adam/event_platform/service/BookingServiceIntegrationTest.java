package com.adam.event_platform.service;

import com.adam.event_platform.dto.BookingEvent;
import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.exception.InsufficientCapacityException;
import com.adam.event_platform.model.*;
import com.adam.event_platform.repository.BookingRepository;
import com.adam.event_platform.repository.EventRepository;
import com.adam.event_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("testuser", "password", "test@example.com");
        userRepository.save(testUser);

        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setCapacity(2);
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        eventRepository.save(testEvent);
    }

    @Test
    void testSuccessfulBooking() {
        ReserveBookingRequest request = new ReserveBookingRequest(testUser.getId(), testEvent.getId());
        bookingService.reserveBooking(request);

        assertThat(bookingRepository.countTotalBookingsByEventId(testEvent.getId())).isEqualTo(1);
    }

    @Test
    void testOverbookingPrevention() {
        // First booking
        bookingService.reserveBooking(new ReserveBookingRequest(testUser.getId(), testEvent.getId()));
        
        // Second booking (still within capacity)
        User user2 = new User("user2", "password", "user2@example.com");
        userRepository.save(user2);
        bookingService.reserveBooking(new ReserveBookingRequest(user2.getId(), testEvent.getId()));

        // Third booking (should fail)
        User user3 = new User("user3", "password", "user3@example.com");
        userRepository.save(user3);

        assertThrows(InsufficientCapacityException.class, () -> {
            bookingService.reserveBooking(new ReserveBookingRequest(user3.getId(), testEvent.getId()));
        });
    }
}