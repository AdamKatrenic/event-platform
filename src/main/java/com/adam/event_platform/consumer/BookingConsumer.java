package com.adam.event_platform.consumer;

import com.adam.event_platform.dto.BookingEvent;
import com.adam.event_platform.model.Booking;
import com.adam.event_platform.model.BookingStatus;
import com.adam.event_platform.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingConsumer {

    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "booking-events", groupId = "booking-group")
    public void consumeBookingEvent(BookingEvent event) {
        log.info("Received booking event for bookingId: {}", event.getBookingId());

        bookingRepository.findById(event.getBookingId()).ifPresentOrElse(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("Successfully confirmed booking with ID: {}", event.getBookingId());
            } else {
                log.warn("Booking with ID: {} is in status {} and cannot be confirmed.", 
                        event.getBookingId(), booking.getStatus());
            }
        }, () -> log.error("Booking not found for ID: {}", event.getBookingId()));
    }
}