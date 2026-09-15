package com.adam.event_platform.repository;

import com.adam.event_platform.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    List<Event> findByStartTimeAfter(LocalDateTime dateTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    /**
     * Get all events sorted by ID descending (most recent first).
     */
    @Query("SELECT e FROM Event e ORDER BY e.id DESC")
    List<Event> findAllOrderByCreatedAtDesc();

    /**
     * Alternative method for getting all events sorted by ID ascending.
     */
    @Query("SELECT e FROM Event e ORDER BY e.id ASC")
    List<Event> findAllByOrderByCreatedByIdAsc();

    /**
     * Paginated query for listing events.
     */
    Page<Event> findAll(Pageable pageable);
    
    /**
     * Get paginated events sorted by ID descending (most recent first).
     */
    @Query("SELECT e FROM Event e ORDER BY e.id DESC")
    Page<Event> findAllOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Get paginated events sorted by ID ascending.
     */
    @Query("SELECT e FROM Event e ORDER BY e.id ASC")
    Page<Event> findAllByOrderByCreatedByIdAsc(Pageable pageable);

    /**
     * Search paginated events by keyword in title.
     */
    Page<Event> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}