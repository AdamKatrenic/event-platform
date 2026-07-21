package com.adam.event_platform.controller;

import com.adam.event_platform.dto.BookingResponse;
import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing booking-related API endpoints.
 *
 * Note: no try/catch here. BookingService throws ResourceNotFoundException,
 * InsufficientCapacityException, or InvalidBookingStateException - all of
 * them are caught centrally by GlobalExceptionHandler. The controller's
 * only job is to translate HTTP requests into service calls.
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new reservation for one or more ticket categories.
     * POST /api/v1/bookings/reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveBooking(@Valid @RequestBody ReserveBookingRequest request) {
        BookingResponse confirmedBooking = bookingService.reserveBooking(request);
        return new ResponseEntity<>(confirmedBooking, HttpStatus.CREATED);
    }

    /**
     * Lists all bookings (any status) belonging to a user.
     * GET /api/v1/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Cancels an existing (CONFIRMED) booking.
     * DELETE /api/v1/bookings/{bookingId}/cancel
     */
    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId);
        return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
    }
}