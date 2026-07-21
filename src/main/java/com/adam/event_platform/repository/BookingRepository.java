package com.adam.event_platform.repository;

import com.adam.event_platform.model.Booking;
import com.adam.event_platform.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId AND b.status = 'CONFIRMED'")
    long countConfirmedBookingsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId")
    long countTotalBookingsByEventId(@Param("eventId") Long eventId);

    List<Booking> findByUser(User user);
}