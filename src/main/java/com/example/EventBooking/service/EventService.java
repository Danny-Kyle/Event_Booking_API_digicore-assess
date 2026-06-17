package com.example.EventBooking.service;

import java.util.List;

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

    @Transactional
    public List<Event> getAllEvents (){
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById (Long id){
        return eventRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }
}
