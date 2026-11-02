package com.example.EventBooking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EventBooking.dto.EventRequest;
import com.example.EventBooking.exception.ResourceNotFoundException;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.repository.EventRepository;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event createEvent(EventRequest request){
        Event event = new Event(
            request.getTitle(),
            request.getDescription(),
            request.getDate(),
            request.getVenue(),
            request.getTotalSeats()
        );
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<Event> getAllEvents (Pageable pageable){
        return eventRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Event getEventById (Long id){
        return eventRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }
}
