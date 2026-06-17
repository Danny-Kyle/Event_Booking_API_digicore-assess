package com.example.EventBooking.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name= "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String attendeeName;
    private String attendeeEmail;
    private LocalDateTime bookedAt;

    public Booking(){}

    public Booking(Long eventId, String attendeeName, String attendeeEmail){
        this.eventId = eventId;
        this.attendeeEmail = attendeeEmail;
        this.attendeeName = attendeeName;
        this.bookedAt = LocalDateTime.now();
    }

    public Long getId(){return id;}
    public void setId(Long id) {this.id = id;}

    public Long getEventId(){return eventId;}
    public void setEventId(Long eventId){this.eventId = eventId;}

    public String getAttendeeName(){return attendeeName;}
    public void setAttendeeName(String attendeeName) {this.attendeeName = attendeeName;}

    public String getAttendeeEmail(){return attendeeEmail;}
    public void setAttendeeEmail(String attendeeEmail) {this.attendeeEmail = attendeeEmail;};

    public LocalDateTime getBookedAt(){return bookedAt;}
    public void setBookedAt(LocalDateTime bookedAt){this.bookedAt = bookedAt;};
}
