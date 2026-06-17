package com.example.EventBooking.dto;

import jakarta.validation.constraints.*;

public class BookingRequest {
    @NotBlank(message = "Attendee name cannot be left empty!")
    private String attendeeName;

    @NotBlank(message = "Attendee email cannot be left empty!")
    @Email(message = "Attendee email must be in a VALID FORMAT!")
    private String attendeeEmail;

    
    public String getAttendeeName(){return attendeeName;}
    public void setAttendeeName(String attendeeName) {this.attendeeName = attendeeName;}

    public String getAttendeeEmail(){return attendeeEmail;}
    public void setAttendeeEmail(String attendeeEmail) {this.attendeeEmail = attendeeEmail;};
}
