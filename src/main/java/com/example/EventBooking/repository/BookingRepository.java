package com.example.EventBooking.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;
import com.example.EventBooking.model.*;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    //to find and get all the bookings for one event
    List<Booking> findbyEventId(Long eventId);

    //to check if the current email has already booked an event
    boolean existsByEventIdandAttendeeEmail(Long eventId, String attendeeEmail);
}
