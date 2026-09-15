package com.adam.event_platform.controller;

import com.adam.event_platform.dto.CreateEventRequest;
import com.adam.event_platform.model.Event;
import com.adam.event_platform.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * GET all events - plural path for listing.
     */
    @GetMapping("/api/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * GET by ID or title - tries ID first, falls back to title search.
     */
    @GetMapping("/api/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        try {
            Long longId = Long.parseLong(id);
            Event event = eventService.getEventById(longId);
            return ResponseEntity.ok(event);
        } catch (NumberFormatException e) {
            // If parsing fails, it might be a title - search by title
            Event event = eventService.getEventByTitle(id);
            return ResponseEntity.ok(event);
        }
    }

    /**
     * POST to create a new event - singular path for creation.
     */
    @PostMapping("/api/event")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event createdEvent = eventService.createEvent(request);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    /**
     * PUT to update an event by title.
     */
    @PutMapping("/api/events/{title}")
    public ResponseEntity<Event> updateEvent(@PathVariable String title, @Valid @RequestBody CreateEventRequest request) {
        Event updatedEvent = eventService.updateEvent(title, request);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * DELETE to remove an event by title.
     */
    @DeleteMapping("/api/events/{title}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String title) {
        eventService.deleteEvent(title);
        return ResponseEntity.ok().build();  // Return 200 OK instead of 204
    }
}