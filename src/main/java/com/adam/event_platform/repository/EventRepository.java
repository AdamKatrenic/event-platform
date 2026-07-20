package com.adam.event_platform.repository;

import com.adam.event_platform.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}