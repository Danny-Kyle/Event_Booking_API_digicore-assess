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

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.model.Booking;
import com.example.EventBooking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(produces = "application/json")
@Validated
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request or booking rule violation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Event or booking not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
})
@Tag(name = "Booking Management", description="create, manage and cancel seat bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{id}/bookings")
    @ApiResponse(responseCode = "201", description = "Seat booked", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class)))
    @Operation(summary = "Book a seat")
    public ResponseEntity<Booking> createBooking(@PathVariable("id") Long eventId, @Valid @RequestBody BookingRequest request){
        Booking booking = bookingService.createBooking(request, eventId);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @DeleteMapping("/bookings/{id}")
    @ApiResponse(responseCode = "204", description = "Booking cancelled", content = @Content)
    @Operation(summary = "Cancel booking")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id){
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{id}/bookings")
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @Operation(summary = "list all bookings for an event")
    public ResponseEntity<PageResponse<Booking>> getBookingsByEventId(
            @PathVariable("id") Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size){
        return ResponseEntity.ok(PageResponse.from(bookingService.getBookingsByEventId(
                eventId, PageRequest.of(page, size, Sort.by("id")))));
    }
}
