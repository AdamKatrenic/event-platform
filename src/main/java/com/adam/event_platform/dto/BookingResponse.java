package com.adam.event_platform.dto;

/**
 * What the API actually returns for a Booking - decoupled from the JPA
 * entity so the internal DB shape (e.g. lazy-loaded User/Event) never
 * leaks into the HTTP response.
 *
 * Kept minimal to match the current entity. Add totalPrice / createdAt /
 * a list of ticket items here once Booking / TicketCategory grow those
 * fields - this is the one place you'll need to touch.
 */
public record BookingResponse(
        Long id,
        Long userId,
        Long eventId,
        String status
) {}