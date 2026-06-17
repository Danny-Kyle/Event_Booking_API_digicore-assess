package com.example.EventBooking.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDateTime date;
    private String venue;
    private int totalSeats;
    private int bookedSeats;
    private EventStatus status = EventStatus.OPEN;

    public Event(){}

    public Event(String title, String description, LocalDateTime date, String venue, int totalSeats){
        this.title = title;
        this.description = description;
        this.date = date;
        this.venue = venue;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
        this.status = EventStatus.OPEN;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

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

    public int getBookedSeats() {return bookedSeats;}
    public void setBookedSeats(int bookedSeats) {this.bookedSeats = bookedSeats;}

    public EventStatus getStatus() {return status;}
    public void setStatus(EventStatus status) {this.status = status;}

}
