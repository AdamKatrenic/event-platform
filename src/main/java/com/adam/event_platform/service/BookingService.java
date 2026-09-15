package com.adam.event_platform.service;

import com.adam.event_platform.dto.BookingEvent;
import com.adam.event_platform.dto.BookingResponse;
import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.exception.InsufficientCapacityException;
import com.adam.event_platform.exception.InvalidBookingStateException;
import com.adam.event_platform.exception.ResourceNotFoundException;
import com.adam.event_platform.model.*;
import com.adam.event_platform.repository.BookingRepository;
import com.adam.event_platform.repository.EventRepository;
import com.adam.event_platform.repository.UserRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    
    // Kafka template for sending events to Kafka topics
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Inject KafkaTemplate via constructor (Spring dependency injection)
    public BookingService(EventRepository eventRepository, 
                          UserRepository userRepository, 
                          BookingRepository bookingRepository,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public BookingResponse reserveBooking(ReserveBookingRequest request) {
        // Step 1: Find or create user
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + request.userId() + " not found."));

        // Step 2: Find event for booking
        Event event = eventRepository.findByIdForUpdate(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event with ID " + request.eventId() + " not found."));

        // Step 3: Check capacity before creating booking
        long currentActiveBookings = bookingRepository.countActiveBookingsByEventId(request.eventId());
        if (currentActiveBookings >= event.getCapacity()) {
            throw new InsufficientCapacityException(
                    "Event '" + event.getTitle() + "' has reached its maximum capacity of " + event.getCapacity());
        }

        // Step 4: Create and save new booking
        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setEvent(event);
        newBooking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(newBooking);
        
        // Step 5: SEND EVENT TO KAFKA TOPIC (async notification)
        // Fire-and-forget - don't block waiting for Kafka response
        sendBookingConfirmedEvent(saved, user, event);

        return toResponse(saved);
    }

    /**
     * Publish booking confirmation event to Kafka topic "bookings"
     * 
     * WHY ASYNCHRONOUS? 
     * ==================
     * We use fire-and-forget sending because:
     * 1. User should get immediate response (no waiting for Kafka)
     * 2. Kafka might be down temporarily - we still want bookings to work
     * 3. Notification services (email, SMS) can process at their own pace
     */
    private void sendBookingConfirmedEvent(Booking booking, User user, Event event) {
        BookingEvent message = new BookingEvent();
        message.setBookingId(booking.getId());
        message.setUsername(user.getUsername());
        message.setUserEmail(user.getEmail());
        message.setEventName(event.getTitle());
        message.setTimestamp(LocalDateTime.now());
        message.setEventType("CONFIRMED");

        // Fire-and-forget: Send without waiting for response
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "bookings", 
                null,  // partition: null = use default auto-partition strategy  
                String.valueOf(booking.getId()), // key as String
                message
        );
        
        // Fire-and-forget: Send asynchronously without blocking or waiting
        kafkaTemplate.send(record);
        log.info("Booking event sent successfully: {}", booking.getId());
    }

    public List<BookingResponse> getBookingsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));
        return bookingRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking ID " + bookingId + " not found."));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Cannot cancel a booking that is not CONFIRMED. Current status: " + booking.getStatus());
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Step 5: SEND CANCELLATION EVENT TO KAFKA (fire-and-forget)
        sendBookingCancelledEvent(saved);

        return toResponse(saved);
    }

    /**
     * Publish cancellation event to Kafka topic "bookings"
     */
    private void sendBookingCancelledEvent(Booking booking) {
        BookingEvent message = new BookingEvent();
        message.setBookingId(booking.getId());
        
        // Fire-and-forget pattern - find user and event data asynchronously
        Optional<User> optionalUser = userRepository.findById(booking.getUser().getId());
        Optional<Event> optionalEvent = eventRepository.findById(booking.getEvent().getId());

        if (optionalUser.isPresent() && optionalEvent.isPresent()) {
            User user = optionalUser.get();
            Event event = optionalEvent.get();
            
            message.setUsername(user.getUsername());
            message.setUserEmail(user.getEmail());
            message.setEventName(event.getTitle());
            message.setTimestamp(LocalDateTime.now());
            message.setEventType("CANCELLED");

            // Fire-and-forget: Send without waiting for response
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    "bookings", 
                    null,  // partition: null = use default auto-partition strategy
                    String.valueOf(booking.getId()), // key as String
                    message
            );
            
            // Fire-and-forget: Send asynchronously without blocking or waiting
            kafkaTemplate.send(record);
            log.info("Booking cancellation event sent: {}", booking.getId());
        } else {
            // User or Event not found - log warning but don't fail the transaction
            String details = optionalUser.isPresent() && optionalEvent.isPresent() 
                    ? "Both user and event found" 
                    : (optionalUser.isPresent() ? "User found but event not found" 
                            : "Event found but user not found");
            log.warn("Cannot send cancellation event - {}: {}", booking.getId(), details);
        }
    }

    /**
     * Convert Booking to response DTO
     */
    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus().toString()
        );
    }
}