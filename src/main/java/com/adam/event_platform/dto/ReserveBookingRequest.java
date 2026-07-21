package com.adam.event_platform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/v1/bookings/reserve.
 *
 * Matches the current domain model: one booking = one seat at one event
 * (Event has a flat "capacity" field, no TicketCategory yet). When you
 * introduce TicketCategory + BookingItem (multiple ticket types per
 * booking), this record will grow a List<TicketItemRequest> - keep that
 * in mind so the future refactor doesn't surprise you.
 */
public record ReserveBookingRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "eventId is required") Long eventId
) {}