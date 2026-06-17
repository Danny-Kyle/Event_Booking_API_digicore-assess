package com.example.EventBooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.exception.BadRequestException;
import com.example.EventBooking.model.Booking;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.model.EventStatus;
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

    @Test
    void createBooking_ThrowsBadRequest_WhenEventClosed(){
        openEvent.setStatus(EventStatus.CLOSED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(openEvent));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(bookingRequest, 1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ThrowsBadRequest_WhenFullyBooked() {
        openEvent.setBookedSeats(5); // maxed capacity out
        when(eventRepository.findById(1L)).thenReturn(Optional.of(openEvent));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(bookingRequest, 1L));
    }

    @Test
    void createBooking_ThrowsBadRequest_WhenDuplicateEmail() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(openEvent));
        when(bookingRepository.existsByEventIdandAttendeeEmail(1L, "john@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(bookingRequest, 1L));
    }

    @Test
    void createBooking_AutoClosesEvent_WhenLastSeatBooked() {
        openEvent.setTotalSeats(1);
        openEvent.setBookedSeats(0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(openEvent));
        when(bookingRepository.existsByEventIdandAttendeeEmail(1L, "john@example.com")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.createBooking(bookingRequest, 1L);

        assertEquals(1, openEvent.getBookedSeats());
        assertEquals(EventStatus.CLOSED, openEvent.getStatus()); // Bonus rule validated
    }

    @Test
    void cancelBooking_Successful() {
        Booking booking = new Booking(1L, "John Doe", "john@example.com");
        booking.setId(10L);
        openEvent.setBookedSeats(1);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(openEvent));

        bookingService.CancelBooking(10L);

        assertEquals(0, openEvent.getBookedSeats());
        verify(bookingRepository, times(1)).delete(booking);
        verify(eventRepository, times(1)).save(openEvent);
    }
}
