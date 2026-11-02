package com.example.EventBooking.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;
import com.example.EventBooking.model.*;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    //to find and get all the bookings for one event
    Page<Booking> findByEventId(Long eventId, Pageable pageable);

    //to check if the current email has already booked an event
    boolean existsByEventIdAndAttendeeEmail(Long eventId, String attendeeEmail);

    @Query("select b.eventId from Booking b where b.id = :id")
    Optional<Long> findEventIdById(@org.springframework.data.repository.query.Param("id") Long id);
}
