package com.example.EventBooking.controllers;

import com.example.EventBooking.dto.PageResponse;
import com.example.EventBooking.dto.ApiError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Validated
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request or booking rule violation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Event or booking not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
})
@RequestMapping(value = "/events", produces = "application/json")
@Tag(name = "Event Management", description = "Endpoints for managing and viewing events")
public class EventController {
    private final EventService eventService;

    public EventController (EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Event created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class)))
    @Operation(summary = "Create a new event", description = "creates a new event with a capacity limit")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request){
        Event createdEvent = eventService.createEvent(request);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @Operation(summary = "Get event details by ID")
    public ResponseEntity<Event> getEventById (@PathVariable Long id){
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @Operation(summary = "Show all events")
    public ResponseEntity<PageResponse<Event>> getAllEvents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(PageResponse.from(eventService.getAllEvents(
                PageRequest.of(page, size, Sort.by("id")))));
    }
}
