package com.example.EventBooking;

import com.example.EventBooking.dto.BookingRequest;
import com.example.EventBooking.exception.BadRequestException;
import com.example.EventBooking.exception.ResourceNotFoundException;
import com.example.EventBooking.model.Event;
import com.example.EventBooking.model.EventStatus;
import com.example.EventBooking.repository.BookingRepository;
import com.example.EventBooking.repository.EventRepository;
import com.example.EventBooking.service.BookingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventBookingApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EventRepository events;
    @Autowired BookingRepository bookings;
    @Autowired BookingService bookingService;

    @BeforeEach
    void clearDatabase() {
        bookings.deleteAll();
        events.deleteAll();
    }

    @Test
    void eventAndBookingLifecycle() throws Exception {
        long eventId = createEvent(1);
        JsonNode booking = book(eventId, "alex@example.com");
        assertEquals(eventId, booking.get("eventId").asLong());
        assertNotNull(booking.get("bookedAt"));

        mvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookedSeats").value(1))
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(post("/events/{id}/bookings", eventId).contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("other@example.com"))).andExpect(status().isBadRequest());
        mvc.perform(get("/events/{id}/bookings", eventId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(delete("/bookings/{id}", booking.get("id").asLong()))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/events/{id}", eventId))
                .andExpect(jsonPath("$.bookedSeats").value(0)).andExpect(jsonPath("$.status").value("OPEN"));
        mvc.perform(get("/events/{id}/bookings", eventId))
                .andExpect(jsonPath("$.totalElements").value(0));
        book(eventId, "alex@example.com"); // A cancelled email can book again.
    }

    @Test
    void duplicateEmailsAreExactAndScopedToAnEvent() throws Exception {
        long eventId = createEvent(3);
        book(eventId, "Alex@example.com");
        mvc.perform(post("/events/{id}/bookings", eventId).contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("Alex@example.com")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        book(eventId, "alex@example.com");
        book(createEvent(1), "Alex@example.com");
        assertEquals(2, events.findById(eventId).orElseThrow().getBookedSeats());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/events/999999", "/events/999999/bookings"})
    void missingEventsReturn404(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void missingBookingAndBookingOnMissingEventReturn404() throws Exception {
        mvc.perform(delete("/bookings/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(post("/events/999999/bookings").contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson("alex@example.com"))).andExpect(status().isNotFound());
    }

    @Test
    void invalidEventsAndBookingsReturn400WithoutWrites() throws Exception {
        Map<String, Object> request = Map.of("title", " ", "venue", " ", "totalSeats", 0,
                "date", LocalDateTime.now().minusDays(1).toString());
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.venue").exists()).andExpect(jsonPath("$.errors.totalSeats").exists())
                .andExpect(jsonPath("$.errors.date").exists());
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.date").exists());
        assertEquals(0, events.count());
        long eventId = createEvent(2);
        mvc.perform(post("/events/{id}/bookings", eventId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"attendeeName\":\" \",\"attendeeEmail\":\"invalid\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.attendeeEmail").exists())
                .andExpect(jsonPath("$.errors.attendeeName").exists());
        assertEquals(0, bookings.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/events?page=-1", "/events?size=0", "/events?size=101", "/events?page=no", "/events/not-a-number"})
    void invalidParametersReturn400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void malformedBodiesReturn400() throws Exception {
        for (String body : List.of("{", "{\"date\":\"not-a-date\"}", "{\"totalSeats\":\"many\"}")) {
            mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
    }

    @Test
    void listsArePaginatedWithStableIdOrdering() throws Exception {
        long firstId = createEvent(3);
        long secondId = createEvent(1);
        createEvent(1);
        mvc.perform(get("/events?page=1&size=1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1)).andExpect(jsonPath("$.content[0].id").value(secondId))
                .andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.totalPages").value(3));
        mvc.perform(get("/events?page=10&size=1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
        book(firstId, "first@example.com");
        long secondBooking = book(firstId, "second@example.com").get("id").asLong();
        book(secondId, "other@example.com");
        mvc.perform(get("/events/{id}/bookings?page=1&size=1", firstId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(secondBooking))
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/events/{id}/bookings?size=101", firstId)).andExpect(status().isBadRequest());
    }

    @Test
    void concurrentRequestsCannotOverbookTheLastSeat() throws Exception {
        long eventId = createEvent(1);
        List<String> outcomes = race(() -> tryBook(eventId, "one@example.com"),
                () -> tryBook(eventId, "two@example.com"));
        assertEquals(1, outcomes.stream().filter("created"::equals).count());
        assertEquals(1, outcomes.stream().filter("rejected"::equals).count());
        assertEquals(1, bookings.count());
        Event event = events.findById(eventId).orElseThrow();
        assertEquals(1, event.getBookedSeats());
        assertEquals(EventStatus.CLOSED, event.getStatus());
    }

    @Test
    void concurrentDuplicateRequestsOnlyCreateOneBooking() throws Exception {
        long eventId = createEvent(3);
        List<String> outcomes = race(() -> tryBook(eventId, "same@example.com"),
                () -> tryBook(eventId, "same@example.com"));
        assertEquals(1, outcomes.stream().filter("created"::equals).count());
        assertEquals(1, outcomes.stream().filter("rejected"::equals).count());
        assertEquals(1, bookings.count());
        assertEquals(1, events.findById(eventId).orElseThrow().getBookedSeats());
    }

    @Test
    void concurrentCancellationOnlyFreesTheSeatOnce() throws Exception {
        long eventId = createEvent(1);
        long bookingId = book(eventId, "one@example.com").get("id").asLong();
        Callable<String> cancel = () -> {
            try {
                bookingService.cancelBooking(bookingId);
                return "cancelled";
            } catch (ResourceNotFoundException ex) {
                return "missing";
            }
        };
        List<String> outcomes = race(cancel, cancel);
        assertEquals(1, outcomes.stream().filter("cancelled"::equals).count());
        assertEquals(1, outcomes.stream().filter("missing"::equals).count());
        assertEquals(0, bookings.count());
        Event event = events.findById(eventId).orElseThrow();
        assertEquals(0, event.getBookedSeats());
        assertEquals(EventStatus.OPEN, event.getStatus());
    }

    @Test
    void swaggerDocumentsAllEndpointsAndErrorSchemas() throws Exception {
        JsonNode spec = mapper.readTree(mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for (String path : List.of("/events", "/events/{id}", "/events/{id}/bookings", "/bookings/{id}")) {
            assertTrue(spec.get("paths").has(path), path);
        }
        assertTrue(spec.at("/components/schemas").has("ApiError"));
        for (String path : List.of("~1events", "~1events~1{id}", "~1events~1{id}~1bookings")) {
            assertFalse(spec.at("/paths/" + path + "/get/responses/200/content/application~1json/schema").isMissingNode(), path);
            assertEquals("#/components/schemas/ApiError", spec.at("/paths/" + path + "/get/responses/400/content/application~1json/schema/$ref").asText());
        }
        assertTrue(spec.at("/components/schemas/PageResponseEvent/properties").has("content"));
        assertTrue(spec.at("/components/schemas/PageResponseBooking/properties").has("totalElements"));
        assertFalse(spec.at("/paths/~1events/post/responses/201/content/application~1json/schema").isMissingNode());
        assertTrue(spec.at("/paths/~1bookings~1{id}/delete/responses").has("204"));
        assertEquals(2, spec.at("/paths/~1events/get/parameters").size());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    private long createEvent(int seats) throws Exception {
        String body = mapper.writeValueAsString(Map.of("title", "Conference", "description", "API demo",
                "date", LocalDateTime.now().plusDays(2).toString(), "venue", "Lagos", "totalSeats", seats));
        JsonNode event = mapper.readTree(mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.bookedSeats").value(0))
                .andExpect(jsonPath("$.status").value("OPEN")).andReturn().getResponse().getContentAsString());
        return event.get("id").asLong();
    }

    private JsonNode book(long eventId, String email) throws Exception {
        return mapper.readTree(mvc.perform(post("/events/{id}/bookings", eventId).contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(email))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private String bookingJson(String email) throws Exception {
        return mapper.writeValueAsString(Map.of("attendeeName", "Alex", "attendeeEmail", email));
    }

    private String tryBook(long eventId, String email) {
        BookingRequest request = new BookingRequest();
        request.setAttendeeName("Alex");
        request.setAttendeeEmail(email);
        try {
            bookingService.createBooking(request, eventId);
            return "created";
        } catch (BadRequestException ex) {
            return "rejected";
        }
    }

    private List<String> race(Callable<String> first, Callable<String> second) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var futures = List.of(first, second).stream().map(action -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
                return action.call();
            })).toList();
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(futures.get(0).get(15, TimeUnit.SECONDS), futures.get(1).get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
