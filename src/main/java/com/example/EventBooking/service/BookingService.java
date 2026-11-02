package com.example.EventBooking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.exception.ResourceNotFoundException;
import com.example.EventBooking.model.Booking;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.model.EventStatus;
import com.example.EventBooking.repository.BookingRepository;
import com.example.EventBooking.repository.EventRepository;
import com.example.EventBooking.exception.BadRequestException;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public BookingService (BookingRepository bookingRepository, EventRepository eventRepository){
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Booking createBooking(BookingRequest request, Long eventId){
        Event event = eventRepository.findByIdForUpdate(eventId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Event with id " +eventId +" not found"));
        
        if (event.getStatus() == EventStatus.CLOSED) {
            throw new BadRequestException("Cannot book seat, this event is currently closed!!!!");
        }
        
        if (event.getBookedSeats() >=event.getTotalSeats()){
            throw new BadRequestException("Cannot book seat, this event is fully booked!");
        }

        if(bookingRepository.existsByEventIdAndAttendeeEmail(eventId, request.getAttendeeEmail())){
            throw new BadRequestException("This email has already been used to book a seat for this event!");
        }

        Booking booking = new Booking(
            eventId,
            request.getAttendeeName(),
            request.getAttendeeEmail()   
        );
        Booking savedBooking = bookingRepository.save(booking);

        event.setBookedSeats(event.getBookedSeats() + 1);

        if(event.getBookedSeats() == event.getTotalSeats()){
            event.setStatus(EventStatus.CLOSED);
        }

        eventRepository.save(event);
        return savedBooking;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Long eventId = bookingRepository.findEventIdById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id: " + bookingId + " does not exist!"));

        // Both creation and cancellation lock the event first. Load the booking
        // after the lock so two cancellations cannot decrement the same seat twice.
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("No associated event was found for this booking"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id: " + bookingId + " does not exist!"));

        //delete the booking record
        bookingRepository.delete(booking);

        //decrease booked seats total
        event.setBookedSeats(event.getBookedSeats() - 1);

        //if it was full, open it up now that there's a free seat
        if(event.getStatus() == EventStatus.CLOSED && event.getBookedSeats() < event.getTotalSeats()){
            event.setStatus(EventStatus.OPEN);
        }

        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<Booking> getBookingsByEventId(Long eventId, Pageable pageable){
        //verify the event exists
        if(!eventRepository.existsById(eventId)){
            throw new ResourceNotFoundException("Event with id: " + eventId + " not found!");
        }
        return bookingRepository.findByEventId(eventId, pageable);
    }
}

