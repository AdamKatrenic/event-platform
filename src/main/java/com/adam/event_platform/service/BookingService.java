package com.adam.event_platform.service;

import com.adam.event_platform.dto.BookingResponse;
import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.exception.InsufficientCapacityException;
import com.adam.event_platform.exception.InvalidBookingStateException;
import com.adam.event_platform.exception.ResourceNotFoundException;
import com.adam.event_platform.model.*;
import com.adam.event_platform.repository.BookingRepository;
import com.adam.event_platform.repository.EventRepository;
import com.adam.event_platform.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer handling all core business logic for booking reservations.
 * All methods involving state changes are @Transactional to ensure atomicity.
 */
@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    // Single constructor -> @Autowired is optional here, Spring infers it automatically.
    public BookingService(EventRepository eventRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Attempts to reserve a spot for a user at a specified event.
     *
     * Uses a pessimistic write lock on the Event row (see
     * EventRepository#findByIdForUpdate) so that two concurrent requests
     * for the last available spot cannot both pass the capacity check -
     * the second request blocks until the first transaction commits,
     * then re-reads the up-to-date booking count.
     *
     * @throws ResourceNotFoundException    if the User or Event does not exist.
     * @throws InsufficientCapacityException if the Event has reached capacity.
     */
    @Transactional
    public BookingResponse reserveBooking(ReserveBookingRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with ID " + request.userId() + " not found."));

        // Locks the Event row until this transaction commits/rolls back.
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
        newBooking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(newBooking);
        return toResponse(saved);
    }

    /**
     * Retrieves all bookings (any status) for a specific user ID.
     */
    public List<BookingResponse> getBookingsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));
        return bookingRepository.findByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cancels a confirmed booking, freeing up capacity for the event.
     *
     * @throws ResourceNotFoundException     if the booking does not exist.
     * @throws InvalidBookingStateException  if the booking is not currently CONFIRMED.
     */
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

    // Entity -> DTO mapping kept private and in one place, so the rest of
    // the service never has to think about it, and the JPA entity never
    // leaves this class.
    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus().name()
        );
    }
}