# Event Booking REST API

A backend API for creating events, reserving seats, cancelling bookings, and viewing events and attendees. Built for the Digicore developer assessment.

## Technology

- Java 21 or newer (JDK required)
- Spring Boot 3.4.3, Spring Web, Spring Data JPA, Jakarta Validation
- H2 in-memory database
- Maven (wrapper included)
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito, and Spring Boot integration tests

## Build, test, and run

Set `JAVA_HOME` to your JDK installation. From the project root on Windows:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

Alternatively, use an installed Maven with `mvn clean verify` and `mvn spring-boot:run`.
After building, you can also run `java -jar target/EventBooking-0.0.1-SNAPSHOT.jar`.
The Maven wrapper downloads Maven on its first run; dependency downloads require internet access.

The API listens at `http://localhost:8080`.

- [Swagger UI](http://localhost:8080/swagger-ui/index.html): all endpoints, request/response schemas, and interactive requests.
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [H2 console](http://localhost:8080/h2-console): JDBC URL `jdbc:h2:mem:eventdb`, username `sa`, empty password.

The H2 database is in memory: restarting the application clears all events and bookings.

## Endpoints

| Method | Path | Success | Purpose |
| --- | --- | --- | --- |
| POST | `/events` | 201 | Create an event |
| GET | `/events` | 200 | List events with pagination |
| GET | `/events/{id}` | 200 | Get an event |
| POST | `/events/{id}/bookings` | 201 | Book one seat |
| GET | `/events/{id}/bookings` | 200 | List bookings with pagination |
| DELETE | `/bookings/{id}` | 204 | Cancel a booking and free its seat |

Missing events and bookings return HTTP 404. Invalid input, duplicate bookings, closed events, and exhausted capacity return HTTP 400. Cancellation returns an empty response body.

### Create an event

`POST /events`, with `Content-Type: application/json`:

```json
{
  "title": "Developer Conference",
  "description": "An event for backend developers",
  "date": "2099-06-20T10:00:00",
  "venue": "Lagos",
  "totalSeats": 2
}
```

The date must be in the future when the request is made. Title and venue must not be blank, and capacity must be at least 1. Description is optional.

Example response (HTTP 201):

```json
{
  "id": 1,
  "title": "Developer Conference",
  "description": "An event for backend developers",
  "date": "2099-06-20T10:00:00",
  "venue": "Lagos",
  "totalSeats": 2,
  "bookedSeats": 0,
  "status": "OPEN"
}
```

### Book a seat

`POST /events/1/bookings`:

```json
{
  "attendeeName": "Alex Doe",
  "attendeeEmail": "alex@example.com"
}
```

Example response (HTTP 201; IDs and booking time are generated):

```json
{
  "id": 1,
  "eventId": 1,
  "attendeeName": "Alex Doe",
  "attendeeEmail": "alex@example.com",
  "bookedAt": "2026-11-02T16:00:00"
}
```

Both attendee fields are required and the email must be valid. Each booking reserves exactly one seat. A duplicate email on the same event is rejected. Emails are compared exactly, including letter case; `Alex@example.com` and `alex@example.com` are different. The same email may book different events, and may book the same event again after cancellation.

### Pagination

Both list endpoints accept `page` (zero-based, default `0`) and `size` (default `20`, between `1` and `100`). Results are ordered by ascending ID. Negative pages, invalid sizes, and non-numeric values return HTTP 400. A page beyond the last page returns empty `content` with the total counts.

Examples: `GET /events?page=0&size=10` and `GET /events/1/bookings?page=0&size=10`.

The list response is a pagination object (rather than the earlier bare array):

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

`content` contains the event or booking objects shown above.

### Error responses

```json
{
  "timestamp": "2026-11-02T16:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: 999",
  "errors": {}
}
```

Validation errors use the same structure with status `400`, message `Validation failed`, and field messages in `errors`. Malformed JSON, invalid field types, and invalid path/query values also return HTTP 400.

## Design decisions

- Controllers handle HTTP and annotation validation; services enforce booking rules; repositories handle H2 persistence.
- New events start `OPEN` with zero booked seats. Taking the last seat closes the event. Cancelling a booking reopens it when capacity becomes available.
- Booking and cancellation run in transactions and acquire a pessimistic write lock on the event row. Requests for the same event are serialized so capacity, duplicate checks, and seat counts remain consistent. Separate events can be updated independently.
- Cancellation reads the booking after obtaining the event lock, preventing concurrent cancellations from freeing one seat twice.
- Date/time fields use ISO local date-time values without offsets, interpreted in the server's local timezone. Future-date validation applies at event creation, as specified by the assessment.
- H2 and its console are enabled for local assessment use. There is no authentication layer in this assignment.
- Pagination has a small, explicit response DTO so clients do not depend on Spring Data internals.

## Verification

`clean verify` runs the service unit tests, application startup test, and HTTP integration tests. Coverage includes all six operations, 400/404 responses, validation, exact-email duplicates, pagination, Swagger schemas, automatic close/reopen behavior, and concurrent booking/cancellation requests against H2.
