package com.example.EventBooking.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.model.Booking;
import com.example.EventBooking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Booking Management", description="create, manage and cancel seat bookings")
public class Bookingcontroller {
    private final BookingService bookingService;

    public Bookingcontroller(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{id}/bookings")
    @Operation(summary = "Book a seat")
    public ResponseEntity<Booking> createBooking(@PathVariable("id") Long eventId, @Valid @RequestBody BookingRequest request){
        Booking booking = bookingService.createBooking(request, eventId);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @DeleteMapping("/bookings/{id}")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id){
        bookingService.CancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{id}/bookings")
    @Operation(summary = "list all bookings for an event")
    public ResponseEntity<List<Booking>> getBookingbyEventId(@PathVariable("id") Long eventId){
        return ResponseEntity.ok(bookingService.getBookingsByEventId(eventId));
    }
}
