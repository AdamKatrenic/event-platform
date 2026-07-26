package com.adam.event_platform.service;

import com.adam.event_platform.model.Event;
import com.adam.event_platform.repository.EventRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Cacheable(value = "events", key = "#eventId")
    public Event getEventById(Long eventId) {
        System.out.println("--- [CACHE MISS] Fetching Event " + eventId + " from Database ---");
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }
}