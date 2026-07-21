package com.adam.event_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship 1: Links this booking to a specific user (who made the reservation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relationship 2: Links this booking to a specific event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // The time the reservation was recorded (useful for audit logs)
    private LocalDateTime bookingTime = LocalDateTime.now(); 

    @Enumerated(EnumType.STRING) // Stores BOOKING_STATUS as a string in DB
    @Column(nullable = false)
    private BookingStatus status;

    /**
     * Default constructor required by JPA implementation.
     */
    public Booking() {
        // Initialization logic if necessary, but kept empty for standard use.
    }

    // --- Full Setters and Getters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return User who made the booking
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the User associated with this booking.
     * @param user The user object.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Event that was booked
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Sets the Event associated with this booking.
     * @param event The event object.
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Returns the timestamp when the booking record was created.
     */
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    // We typically don't set 'bookingTime' manually via setter
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    /**
     * @return The current status of the booking (e.g., CONFIRMED, CANCELLED).
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * Sets the booking status.
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
