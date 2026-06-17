package com.example.EventBooking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventRequest {
    @NotBlank(message = "Title is required!")
    private String title;

    private String description;

    @NotNull(message = "event date is required!")
    @Future(message = "event date must be in the future!")
    private LocalDateTime date;

    @NotBlank(message = "venue is required!")
    private String venue;

    @Min(value = 1, message = "Total seats must be at least 1!")
    private int totalSeats;

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public LocalDateTime getDate() {return date;}
    public void setDate(LocalDateTime date) {this.date = date;}

    public String getVenue() {return venue;}
    public void setVenue(String venue) {this.venue = venue;}

    public int getTotalSeats() {return totalSeats;}
    public void setTotalSeats(int totalSeats) {this.totalSeats = totalSeats;}
}
