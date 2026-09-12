package com.adam.event_platform.service;

import com.adam.event_platform.dto.EventRequest;
import com.adam.event_platform.exception.ResourceNotFoundException;
import com.adam.event_platform.model.Event;
import com.adam.event_platform.repository.EventRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Cacheable(value = "events", key = "#eventId")
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));
    }

    public Event getEventByTitle(String title) {
        List<Event> events = eventRepository.findByTitleContainingIgnoreCase(title);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Event not found with title: " + title);
        }
        return events.get(0);
    }

    /**
     * Get all events sorted by creation date descending (most recent first).
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAllOrderByCreatedAtDesc();
    }

    public Event createEvent(EventRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setCapacity(request.capacity());
        event.setStartTime(request.startTime());
        event.setCreatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event updateEvent(String title, EventRequest request) {
        Event existingEvent = getEventByTitle(title);
        existingEvent.setTitle(request.title());
        existingEvent.setDescription(request.description());
        existingEvent.setCapacity(request.capacity());
        existingEvent.setStartTime(request.startTime());
        // Preserve the original creation timestamp
        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    public void deleteEvent(String title) {
        Event existingEvent = getEventByTitle(title);
        eventRepository.delete(existingEvent);
    }
}