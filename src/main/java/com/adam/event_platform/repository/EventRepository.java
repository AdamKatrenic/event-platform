package com.adam.event_platform.repository;

import com.adam.event_platform.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findByDateAfter(LocalDate date);

    /**
     * Same lookup as findById, but takes a PESSIMISTIC_WRITE lock on the
     * row until the surrounding @Transactional method commits or rolls
     * back. Any other transaction calling this on the same eventId blocks
     * until the lock is released, so two concurrent reservation requests
     * for the same event can never both read a stale booking count.
     *
     * Only use this where you're about to check-then-write capacity
     * (e.g. BookingService#reserveBooking). Plain findById() is fine
     * everywhere else - locking has a cost, don't reach for it by default.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}