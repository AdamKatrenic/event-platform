package com.adam.event_platform.controller;

import com.adam.event_platform.dto.BookingResponse;
import com.adam.event_platform.dto.ReserveBookingRequest;
import com.adam.event_platform.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveBooking(@Valid @RequestBody ReserveBookingRequest request) {
        BookingResponse confirmedBooking = bookingService.reserveBooking(request);
        return new ResponseEntity<>(confirmedBooking, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings); // Simplified syntax
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(cancelledBooking); // Simplified syntax
    }
}