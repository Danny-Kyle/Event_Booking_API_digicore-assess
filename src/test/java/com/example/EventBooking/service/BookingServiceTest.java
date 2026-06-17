package com.example.EventBooking.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.repository.BookingRepository;
import com.example.EventBooking.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BookingService bookingService;

    private Event openEvent;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setup(){
        openEvent = new Event("Digi Core Assessment", "Api Conf", LocalDateTime.now().plusDays(2), "Lagos", 5);
        openEvent.setId(1L);

        bookingRequest = new BookingRequest();
        bookingRequest.setAttendeeEmail("chidiebubeokechukwu21@gmail.com");
        bookingRequest.setAttendeeName("Okechukwu Daniel");
    }
}
