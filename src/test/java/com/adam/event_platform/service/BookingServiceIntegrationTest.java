package com.adam.event_platform.service;

import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.exception.InsufficientCapacityException;
import com.adam.event_platform.model.*;
import com.adam.event_platform.repository.BookingRepository;
import com.adam.event_platform.repository.EventRepository;
import com.adam.event_platform.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Mock KafkaTemplate to prevent actual Kafka connections during tests
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Clear all entities before each test to avoid constraint violations
        try {
            bookingRepository.deleteAll();
            eventRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup exceptions from previous failed tests
        }

        // Create a new user with unique email for each test setup
        testUser = new User("testuser", "password", "test_user_" + System.nanoTime() + "@example.com");
        userRepository.save(testUser);

        // Create event with capacity of 2
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setCapacity(2);
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        eventRepository.save(testEvent);
    }

    @AfterEach
    void tearDown() {
        try {
            bookingRepository.deleteAll();
            eventRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup exceptions from previous failed tests
        }
    }

    @Test
    void testSuccessfulBooking() {
        ReserveBookingRequest request = new ReserveBookingRequest(testUser.getId(), testEvent.getId());
        bookingService.reserveBooking(request);

        assertThat(bookingRepository.countActiveBookingsByEventId(testEvent.getId())).isEqualTo(1);
    }

    @Test
    void testOverbookingPrevention() {
        // First booking succeeds
        bookingService.reserveBooking(new ReserveBookingRequest(testUser.getId(), testEvent.getId()));

        // Second user can also book (capacity is 2)
        User user2 = new User("user2", "password", "user2_" + System.nanoTime() + "@example.com");
        userRepository.save(user2);
        bookingService.reserveBooking(new ReserveBookingRequest(user2.getId(), testEvent.getId()));

        // Verify both bookings exist
        assertThat(bookingRepository.countActiveBookingsByEventId(testEvent.getId())).isEqualTo(2);

        // Third user should fail due to capacity limit
        User user3 = new User("user3", "password", "user3_" + System.nanoTime() + "@example.com");
        userRepository.save(user3);

        assertThrows(InsufficientCapacityException.class, () -> {
            bookingService.reserveBooking(new ReserveBookingRequest(user3.getId(), testEvent.getId()));
        });
    }
}