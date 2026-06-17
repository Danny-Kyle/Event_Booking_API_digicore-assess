package com.example.EventBooking.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EventBooking.dto.EventRequest;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
@Tag(name = "Event Management", description = "Endpoints for managing and viewing events")
public class EventController {
    private final EventService eventService;

    public EventController (EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = "Create a new event", description = "creates a new event with a capacity limit")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request){
        Event createdEvent = eventService.createEvent(request);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event details by ID")
    public ResponseEntity<Event> getEventById (@PathVariable Long id){
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping
    @Operation(summary = "Show all events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }
}
