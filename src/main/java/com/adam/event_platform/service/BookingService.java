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

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public BookingService(EventRepository eventRepository, UserRepository userRepository, BookingRepository bookingRepository, KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public BookingResponse reserveBooking(ReserveBookingRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + request.userId() + " not found."));

        Event event = eventRepository.findByIdForUpdate(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event with ID " + request.eventId() + " not found."));

        long currentBookings = bookingRepository.countConfirmedBookingsByEventId(request.eventId());
        if (currentBookings >= event.getCapacity()) {
            throw new InsufficientCapacityException(
                    "Event '" + event.getTitle() + "' has reached its maximum capacity of " + event.getCapacity());
        }

        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setEvent(event);
        newBooking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(newBooking);
        kafkaTemplate.send("booking-events", new BookingEvent(
                saved.getId(),
                user.getId(),
                event.getId()
        ));
        return toResponse(saved);
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

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus().name()
        );
    }
}
