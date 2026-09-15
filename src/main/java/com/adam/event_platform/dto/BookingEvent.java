package com.adam.event_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Kafka messages representing booking events
 * 
 * This object is serialized to JSON and sent over Kafka topics.
 * Each field tracks important information about the event.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {
    
    /** Unique identifier for this event message */
    private Long bookingId;
    
    /** User who made the booking */
    private String username;
    
    /** Email for notification purposes */
    private String userEmail;
    
    /** Event being booked */
    private String eventName;
    
    /** When the booking was created/updated */
    private LocalDateTime timestamp;
    
    /** Status of the booking event (CONFIRMED, CANCELLED, etc.) */
    private String eventType;
}
