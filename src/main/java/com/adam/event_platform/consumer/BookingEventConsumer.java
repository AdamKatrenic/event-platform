package com.adam.event_platform.consumer;

import com.adam.event_platform.dto.BookingEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer - Handles incoming booking events from Kafka topics
 * 
 * WHY DO WE NEED A CONSUMER?
 * ===========================
 * Kafka is publish-subscribe: multiple consumers can listen to the same topic.
 * Common use cases:
 * 1. Email service consumes "confirmed" events → sends confirmation emails
 * 2. Analytics service consumes all events → tracks booking trends
 * 3. SMS service consumes events → sends text notifications
 * 
 * This consumer handles both CONFIRMED and CANCELLED booking events.
 */
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class BookingEventConsumer {

    /**
     * Listen to "bookings" topic on "confirmed" partition
     * Triggers whenever a booking is created/confirmed
     * 
     * @param event The booking confirmation event from Kafka
     */
    @KafkaListener(
        topics = "bookings",          // Kafka topic name we're listening to
        groupId = "booking-events-no-retry"   // Separate consumer group to prevent re-processing loops
    )
    public void handleBookingConfirmed(BookingEvent event) {
        System.out.println("=== BOOKING CONFIRMATION NOTIFICATION ===");
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("User: " + event.getUsername() + 
                           " (" + event.getUserEmail() + ")");
        System.out.println("Event: " + event.getEventName());
        System.out.println("Timestamp: " + event.getTimestamp());
        
        // TODO: Here you would integrate with email service
        // e.g., emailService.sendConfirmation(event.getUserEmail(), event.getEventName());
        
        // TODO: You could also update analytics or send SMS here
        System.out.println("==========================================");
    }

    /**
     * Listen to "bookings" topic on "cancelled" partition  
     * Triggers whenever a booking is cancelled
     */
    @KafkaListener(
        topics = "bookings",          // Same topic, different logical partition via message key
        groupId = "booking-events-no-retry"   // Separate consumer group to prevent re-processing loops
    )
    public void handleBookingCancelled(BookingEvent event) {
        System.out.println("=== BOOKING CANCELLATION NOTIFICATION ===");
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("User: " + event.getUsername() + 
                           " (" + event.getUserEmail() + ")");
        System.out.println("Event: " + event.getEventName());
        System.out.println("Status: CANCELLED");
        System.out.println("Timestamp: " + event.getTimestamp());
        
        // TODO: Send cancellation email/SMS here
        // e.g., emailService.sendCancellation(event.getUserEmail(), event.getBookingId());
        
        System.out.println("==========================================");
    }
}