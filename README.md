# Event Booking REST API

A robust, lightweight backend RESTful service built with Spring Boot to handle event management and attendee seat bookings.
This application utilizes an in-memory database configuration with strict business constraint validation layers to manage concurrent operations, handle capacity caps, and enforce idempotency rules.

## Tech Stack
- **Language** Java 17+
- **Framework:** Spring Boot 3.x (Web, Data JPA, Validation)
- **Database:** H2 Database Engine (In-Memory)
- **Documentation:** OpenAPI / Swagger UI 3 (via Springdoc)
- **Build Engine:** Maven

---

## Design Decisions & Assumptions
1. **Layered Architecture:** The codebase follows a clean 3-tier architecture (Controller ➔ Service ➔ Repository) utilizing explicit Data Transfer Objects (DTOs) to isolate incoming data constraints from persistent schema entities.
2. **H2 Over In-Memory Collections:** Selecting H2 Database via JPA over traditional in-memory HashMaps drastically drops boilerplate structural sync code, provides effortless standard schema definitions, and satisfies the assignment's technical bonus objective.
3. **Data Integrity Guardrails:** Service methods are annotated with `@Transactional` to guarantee that atomic business cycles (e.g., executing a user booking record and safely bumping up the current seat counters on the event tracking tables) never finish partial execution during mid-runtime interruptions.
4. **Automatic Lifecycles:** As a bonus option constraint, when a new booking takes the final vacant slot, the API safely forces the event status to `CLOSED`. Reversely, if a slot gets cancelled later, it scales the tracking flags straight back to `OPEN`.

---

## Architecture Flow Overview
`Client/Swagger` ➔ `Validation Check (DTO)` ➔ `Service Constraints Engine` ➔ `H2 Storage Layer`

---

## Setting Up and Running the Application

### 1. Build the Project
Ensure you have Maven installed. Run the command inside the root folder:
```bash
mvn clean package

```

### 2. Launch the Application

Run the executable Spring Boot package via your terminal console:

```bash
mvn spring-boot:run

```

The application will boot up and begin listening to web traffic requests on default port `8080`.

---

## Access Points & Visual Portals

### 🚀 API Playground (Swagger UI)

Once launched, open up your web browser tracking address to visually test endpoints:

* **Swagger URL:** [http://localhost:8080/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui/index.html)

### 📊 In-Memory Database Console

Access the live underlying database records via the H2 graphics panel layout:

* **H2 Portal URL:** [http://localhost:8080/h2-console](https://www.google.com/search?q=http://localhost:8080/h2-console)
* **JDBC URL to enter:** `jdbc:h2:mem:eventdb`
* **Username:** `sa`
* **Password:** *(Leave completely blank)*

---

## API Endpoints Reference

### Events Endpoint Tree

* `POST /events` - Registers a new scheduling entry *(Requires future date and total seats > 0)*
* `GET /events` - Lists all tracking events saved in system memory.
* `GET /events/{id}` - Extracts comprehensive detail stats for a specific event entry.

### Bookings Endpoint Tree

* `POST /events/{id}/bookings` - Registers an attendee slot reservation *(Blocks closed, sold-out, or duplicate email requests)*
* `GET /events/{id}/bookings` - Pulls historical audit lists of attendees for the event block.
* `DELETE /bookings/{id}` - Releases a reservation block and un-allocates the occupied seat capacity.

```

```