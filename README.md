# Reservation Management System

A production-style backend reservation management application built with Spring Boot, PostgreSQL, Spring Security, JWT authentication, and Docker.

The system provides secure REST APIs for managing rooms and reservations while implementing role-based authorization, validation, centralized exception handling, structured logging, correlation IDs, automated testing, and containerized deployment.
[![CI](https://github.com/YogendraPattabhiMekala/reservation-system/actions/workflows/ci.yml/badge.svg)](https://github.com/YogendraPattabhiMekala/reservation-system/actions/workflows/ci.yml)
## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- PostgreSQL
- H2 Database for Testing
- Maven
- Docker
- Docker Compose
- Swagger / OpenAPI
- JUnit 5
- Mockito
- MockMvc
- JaCoCo

## Core Features

- User registration and authentication
- JWT-based stateless security
- Role-based access control
- Room management
- Reservation creation and management
- Room availability checking
- Reservation conflict prevention
- Pagination, sorting, and dynamic filtering
- Request validation
- Centralized exception handling
- Standardized API error responses
- Correlation ID tracking using `X-Correlation-Id`
- MDC-based request tracing
- Request/response logging
- PostgreSQL persistence
- Dockerized application and database
- Separate H2-based test environment
- Unit, controller, repository, security, and integration testing


## System Architecture

The application follows a layered Spring Boot architecture designed to separate API handling, business logic, persistence, security, and cross-cutting concerns.

```text
Client / Swagger UI
        |
        v
CorrelationIdFilter
        |
        v
Spring Security Filter Chain
        |
        v
JWT Authentication Filter
        |
        v
Controllers
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
PostgreSQL

## Security & JWT Authentication

The application uses Spring Security with stateless JWT-based authentication.

### Authentication Flow

```text
User Registration / Login
        |
        v
Credentials validated
        |
        v
JWT token generated
        |
        v
Client stores token
        |
        v
Client sends token in Authorization header
        |
        v
JWT Authentication Filter validates token
        |
        v
Authenticated user is added to SecurityContext
        |
        v
Protected endpoint is processed

## Reservation Business Rules

The reservation service implements business rules to ensure rooms cannot be double-booked and reservation data remains consistent.

### Reservation Conflict Detection

The system prevents overlapping active reservations for the same room.

Two reservations overlap when:

```text
existingCheckIn < requestedCheckOut
AND
existingCheckOut > requestedCheckIn

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate a user and return a JWT token |

### Rooms

| Method | Endpoint | Description |
|---|---|---|
| POST | `/rooms` | Create a new room |
| GET | `/rooms` | Retrieve all rooms |
| GET | `/rooms/{id}` | Retrieve a room by ID |
| GET | `/rooms/type/{roomType}` | Retrieve rooms by room type |
| PUT | `/rooms/{id}` | Update an existing room |
| DELETE | `/rooms/{id}` | Delete a room |
| GET | `/rooms/available` | Find available rooms for a room type and date range |

Example availability request:

```http
GET /rooms/available?roomType=Deluxe&checkInDate=2026-08-20&checkOutDate=2026-08-25

## Error Handling

The application uses centralized exception handling to provide consistent and meaningful API error responses.

The global exception-handling layer handles scenarios such as:

- Resource not found
- Invalid request data
- Bean validation failures
- Malformed JSON requests
- Reservation conflicts
- Invalid reservation operations
- Authentication and authorization failures
- Unexpected application errors

This keeps exception-handling logic out of individual controllers and provides a consistent API experience.

### Example Error Response

```json
{
  "timestamp": "2026-08-21T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Check-out date must be after check-in date",
  "path": "/reservation"
}
```

## Observability and Request Tracing

The application implements correlation-ID-based request tracing to make API requests easier to track across application logs.

### Correlation ID

Every HTTP request uses the following header:

```http
X-Correlation-Id
```

If the client provides a correlation ID, the application preserves it.

If the header is missing or blank, the application automatically generates a UUID for the request.

The same correlation ID is returned in the HTTP response.

```text
Client Request
      |
      | X-Correlation-Id (optional)
      v
CorrelationIdFilter
      |
      +-- Preserve existing ID
      |        OR
      +-- Generate UUID
      |
      v
MDC
      |
      v
Application Processing
      |
      v
Response
X-Correlation-Id: <correlation-id>
```

### MDC Logging

The correlation ID is stored in the SLF4J MDC while the request is being processed, allowing log entries generated during that request to be associated with the same request identifier.

The MDC value is removed after request processing to prevent correlation IDs from leaking between requests handled by reused server threads.

### Request Performance Logging

The request filter also records execution time for HTTP requests.

Example:

```text
HTTP request completed:
method=POST,
path=/reservation,
status=201,
durationMs=12
```

Each completion log captures:

- HTTP method
- Request path
- HTTP response status
- Request processing duration
- Correlation ID through MDC

Sensitive information such as passwords, JWT tokens, authorization headers, and request bodies is not included in request-completion logs.

## Testing Strategy

The application includes a comprehensive automated test suite covering the controller, service, repository, security, validation, filter, and integration layers.

### Test Coverage

The test suite includes:

- Unit tests
- Controller tests using MockMvc
- Service-layer tests
- Repository tests
- Security and authorization tests
- Validation tests
- Exception-handling tests
- Reservation conflict tests
- Room availability tests
- Correlation ID filter tests
- End-to-end correlation ID integration tests
- Spring application context tests

### Current Test Status

```text
159 tests passed
0 tests failed
```

The complete test suite is executed as a regression check after application changes.

### Test Database

Automated tests use an isolated H2 in-memory database instead of the production PostgreSQL database.

The test environment is configured through:

```text
src/test/resources/application-test.properties
```

Tests requiring the application context use the `test` Spring profile so that test execution remains isolated from local and containerized PostgreSQL environments.

### Testing Tools

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc
- Spring Security Test
- H2 Database
- JaCoCo

### Areas Tested

#### Authentication and Security

Tests verify:

- User registration
- User login
- JWT authentication
- Protected endpoint access
- Role-based authorization
- Unauthorized requests
- Forbidden operations

#### Reservation Management

Tests verify:

- Reservation creation
- Reservation retrieval
- Reservation updates
- Reservation deletion
- Reservation cancellation
- Date validation
- Reservation status handling
- Overlapping reservation prevention
- Pagination, sorting, and filtering

#### Room Management

Tests verify:

- Room creation
- Room retrieval
- Room updates
- Room deletion
- Room-type filtering
- Room availability searches
- Exclusion of unavailable rooms
- Exclusion of rooms with overlapping reservations

#### Observability

Tests verify:

- Automatic correlation ID generation
- Preservation of client-provided correlation IDs
- `X-Correlation-Id` response headers
- MDC cleanup after request processing
- Correlation ID behavior through the HTTP request pipeline

### Regression Testing

After significant changes, the complete test suite is executed to ensure existing functionality remains stable.

Current regression baseline:

```text
159 / 159 tests passing
```
## CI/CD and Code Quality

The project uses GitHub Actions to automatically validate changes pushed to the repository and pull requests targeting the `main` branch.

### Continuous Integration

The CI pipeline performs automated build and verification checks before changes can be merged.

The pipeline validates:

- Java 21 build compatibility
- Maven dependency resolution
- Automated test execution
- JaCoCo code coverage requirements
- OWASP dependency vulnerability scanning

The `main` branch is protected and changes are integrated through pull requests after required CI checks pass.

### Code Coverage

JaCoCo is integrated into the Maven build to measure automated test coverage.

Current project coverage is approximately:

```text
Instruction Coverage: ~89%
Branch Coverage:      ~83%
## Docker and Local Development

The application is fully containerized using Docker and Docker Compose.

Docker Compose runs the Spring Boot application and PostgreSQL database as separate containers connected through an internal Docker network.

### Container Architecture

```text
                    Docker Compose
                         |
          +--------------+--------------+
          |                             |
          v                             v
+---------------------+       +---------------------+
| Reservation System  |       | PostgreSQL          |
| Spring Boot         |------>| Database            |
| Port: 8080          |       | Port: 5432          |
+---------------------+       +---------------------+
```

The Spring Boot container connects to PostgreSQL using the Docker Compose service name rather than `localhost`.

```text
jdbc:postgresql://postgres:5432/reservation_system
```

### Prerequisites

Before running the application, install:

- Java 21
- Maven
- Docker Desktop
- Docker Compose
- Git

Java and Maven are required for local development. Docker can be used to run the complete application stack without manually configuring PostgreSQL.

### Environment Variables

Runtime configuration is externalized using environment variables.

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/reservation_system` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `DDL_AUTO` | Hibernate schema behavior | `update` |
| `SHOW_SQL` | Enables Hibernate SQL logging | `true` |
| `SERVER_PORT` | Application HTTP port | `8080` |

This allows the same application artifact to run in local, test, containerized, and deployment environments without changing application code.

### Run with Docker Compose

From the project root, validate the Compose configuration:

```bash
docker compose config
```

Build the application image:

```bash
docker compose build
```

Start the complete application stack:

```bash
docker compose up
```

Docker Compose starts:

1. PostgreSQL
2. PostgreSQL health check
3. Reservation System application after the database becomes healthy

### Run in Detached Mode

To run the containers in the background:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs reservation-app
```

Follow application logs continuously:

```bash
docker compose logs -f reservation-app
```

### Stop the Application

Stop and remove the containers:

```bash
docker compose down
```

The PostgreSQL database uses a Docker volume, allowing database data to persist when containers are recreated.

### Swagger UI

After the application starts successfully, interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger can be used to explore endpoints, authenticate using JWT, and test API operations.

### Run Locally

The application can also be run directly without Docker.

Ensure PostgreSQL is available and configure the appropriate database environment variables.

Then run:

```bash
mvn spring-boot:run
```

Alternatively, run `ReservationSystemApplication` directly from IntelliJ IDEA.

### Run Automated Tests

Execute the complete test suite with:

```bash
mvn test
```

The automated test environment uses H2 and the Spring `test` profile, keeping tests isolated from the PostgreSQL development database.

Current regression baseline:

```text
159 / 159 tests passing
```

### Build the Application

Create the executable Spring Boot JAR with:

```bash
mvn clean package
```

The generated application artifact is placed in:

```text
target/
```

## Project Structure

The project follows a layered package structure to keep responsibilities separated and make the application easier to maintain, test, and extend.

```text
reservation-system/
|
├── src/
│   ├── main/
│   │   ├── java/com/yogendra/reservation_system/
│   │   │   |
│   │   │   ├── controller/
│   │   │   │   ├── AuthController
│   │   │   │   ├── ReservationController
│   │   │   │   └── RoomController
│   │   │   |
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest
│   │   │   │   ├── RegisterRequest
│   │   │   │   ├── ReservationRequest
│   │   │   │   ├── ReservationResponse
│   │   │   │   ├── RoomRequest
│   │   │   │   └── RoomResponse
│   │   │   |
│   │   │   ├── entity/
│   │   │   │   ├── Reservation
│   │   │   │   ├── Room
│   │   │   │   └── User
│   │   │   |
│   │   │   ├── repository/
│   │   │   │   ├── ReservationRepository
│   │   │   │   ├── RoomRepository
│   │   │   │   └── UserRepository
│   │   │   |
│   │   │   ├── service/
│   │   │   │   └── Business service interfaces
│   │   │   |
│   │   │   ├── service/impl/
│   │   │   │   └── Service implementations
│   │   │   |
│   │   │   ├── security/
│   │   │   │   └── JWT and Spring Security components
│   │   │   |
│   │   │   ├── filter/
│   │   │   │   └── CorrelationIdFilter
│   │   │   |
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler
│   │   │   │   └── Custom application exceptions
│   │   │   |
│   │   │   └── ReservationSystemApplication
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/
│       │   └── com/yogendra/reservation_system/
│       │       ├── controller/
│       │       ├── repository/
│       │       ├── service/
│       │       ├── security/
│       │       ├── filter/
│       │       └── integration/
│       │
│       └── resources/
│           └── application-test.properties
│
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .gitignore
├── pom.xml
└── README.md
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `controller` | Exposes REST API endpoints and handles HTTP requests/responses |
| `dto` | Defines request and response models used by the API |
| `entity` | Defines JPA entities persisted in the database |
| `repository` | Provides database access through Spring Data JPA |
| `service` | Defines application business operations |
| `service.impl` | Implements reservation and room business logic |
| `security` | Handles JWT authentication and Spring Security configuration |
| `filter` | Handles cross-cutting HTTP request processing such as correlation IDs and request timing |
| `exception` | Centralizes exception handling and API error responses |
| `test` | Contains automated unit, integration, repository, controller, and security tests |

## Design Principles

The application follows several backend engineering principles:

- **Separation of Concerns** — controllers, business logic, persistence, security, and infrastructure concerns are separated into dedicated layers.
- **Dependency Injection** — Spring manages application dependencies instead of classes manually creating their dependencies.
- **DTO Pattern** — API request and response objects are separated from persistence entities.
- **Repository Pattern** — database access is abstracted through Spring Data JPA repositories.
- **Centralized Error Handling** — application exceptions are translated into consistent HTTP responses from a central handler.
- **Stateless Security** — authentication is performed using JWT without server-side login sessions.
- **Environment-Based Configuration** — deployment-specific settings can be supplied without modifying application code.
- **Test Isolation** — automated tests use a dedicated test configuration and H2 database.
- **Observability** — correlation IDs and request timing provide traceability across HTTP requests.

## Database Model

The application uses PostgreSQL as its primary relational database and Spring Data JPA/Hibernate for persistence.

The core domain consists of three primary entities:

- `User`
- `Room`
- `Reservation`

### Entity Relationships

```text
+------------------+
|       User       |
+------------------+
| id               |
| username         |
| email            |
| password         |
| role             |
+--------+---------+
         |
         | 1
         |
         | *
+--------v---------+
|   Reservation    |
+------------------+
| id               |
| customerName     |
| roomType         |
| checkInDate      |
| checkOutDate     |
| status           |
| createdAt        |
| updatedAt        |
| userId           |
| roomId           |
+--------+---------+
         |
         | *
         |
         | 1
+--------v---------+
|       Room       |
+------------------+
| id               |
| roomNumber       |
| roomType         |
| available        |
+------------------+
```

### User

The `User` entity stores application users used for authentication and reservation ownership.

Important fields include:

- Unique username
- Unique email address
- BCrypt-encoded password
- User role

Users authenticate through the JWT security system before accessing protected application functionality.

### Room

The `Room` entity represents rooms that can be managed and reserved.

Important fields include:

- Unique room number
- Room type
- Availability status

Room availability searches combine the room's availability flag with existing reservations for the requested date range.

### Reservation

The `Reservation` entity represents a booking made by an authenticated user.

Important fields include:

- Customer name
- Room type
- Check-in date
- Check-out date
- Reservation status
- Associated room
- Associated user
- Creation timestamp
- Update timestamp

### Relationships

A reservation is associated with:

```text
Reservation ---> User
Reservation ---> Room
```

This allows the application to track both the authenticated user who owns the reservation and the specific room being reserved.

### Database Constraints

The persistence layer uses database and application-level constraints to protect data integrity.

Examples include:

- Unique usernames
- Unique email addresses
- Unique room numbers
- Required room information
- Required reservation dates
- Foreign-key relationships between reservations, users, and rooms
- Validation of reservation date ranges
- Prevention of overlapping active reservations

### Reservation Conflict Query

Room booking conflicts are detected using date-range overlap logic.

Conceptually:

```text
existingCheckIn < requestedCheckOut
AND
existingCheckOut > requestedCheckIn
```

Combined with the room and active reservation status, this prevents the same room from being double-booked for overlapping periods.

## Example API Workflow

A typical reservation flow looks like this:

```text
Register User
    |
    v
Login
    |
    v
Receive JWT
    |
    v
Create / Manage Room
    |
    v
Search Available Rooms
    |
    v
Create Reservation
    |
    v
Retrieve / Update Reservation
    |
    v
Cancel Reservation
```

### 1. Register a User

```http
POST /auth/register
Content-Type: application/json
```

Example request:

```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "Password123"
}
```

### 2. Login

```http
POST /auth/login
Content-Type: application/json
```

Example request:

```json
{
  "username": "john",
  "password": "Password123"
}
```

The response returns a JWT token that is used for protected API requests.

### 3. Send the JWT

Protected requests include:

```http
Authorization: Bearer <JWT_TOKEN>
```

### 4. Create a Room

Example:

```http
POST /rooms
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "roomNumber": "201",
  "roomType": "Deluxe",
  "available": true
}
```

### 5. Search for Available Rooms

```http
GET /rooms/available?roomType=Deluxe&checkInDate=2026-08-20&checkOutDate=2026-08-25
Authorization: Bearer <JWT_TOKEN>
```

The service checks:

- Room type
- Room availability flag
- Requested dates
- Existing overlapping reservations
- Active reservation status

### 6. Create a Reservation

```http
POST /reservation
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

Example request:

```json
{
  "customerName": "John",
  "roomType": "Deluxe",
  "checkInDate": "2026-08-20",
  "checkOutDate": "2026-08-25",
  "roomId": 1
}
```

The reservation is rejected if an active overlapping booking already exists for the same room.

### 7. Retrieve a Reservation

```http
GET /reservation/{id}
Authorization: Bearer <JWT_TOKEN>
```

### 8. Update a Reservation

```http
PUT /reservation/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

The application re-runs validation and conflict detection before accepting the update.

### 9. Cancel a Reservation

```http
PATCH /reservation/{id}/cancel
Authorization: Bearer <JWT_TOKEN>
```

Cancellation updates the reservation lifecycle while allowing room availability logic to ignore cancelled bookings.

### 10. Trace the Request

Responses include:

```http
X-Correlation-Id: <correlation-id>
```

The same correlation ID is available in application logs, making the request easier to trace.

## Future Improvements

The current system provides a strong backend foundation for reservation management. Future enhancements could include:

- React or TypeScript frontend integration
- Email notifications for reservation confirmations and cancellations
- Refresh-token support for authentication
- Password reset and email verification
- Advanced role and permission management
- Redis caching for frequently accessed data
- Rate limiting for public and authentication endpoints
- Database migrations using Flyway or Liquibase
- Testcontainers-based PostgreSQL integration testing
- Cloud deployment using AWS or Azure
- Container orchestration using Kubernetes
- Metrics and health monitoring using Spring Boot Actuator
- Prometheus and Grafana monitoring
- Centralized application logging
- API versioning
- Improved OpenAPI documentation
- Frontend reservation calendar and availability visualization
> This repository uses GitHub Actions CI and protected pull-request-based development on the `main` branch.
## Project Status

The backend currently includes:

- RESTful room and reservation management
- JWT authentication
- Role-based authorization
- PostgreSQL persistence
- Reservation conflict detection
- Room availability searching
- Pagination, sorting, and dynamic filtering
- Request validation
- Centralized exception handling
- Correlation ID request tracing
- MDC-based logging
- Request performance logging
- Swagger / OpenAPI documentation
- Docker containerization
- Docker Compose orchestration
- Environment-based configuration
- Isolated H2 test environment
- Comprehensive automated testing

### Current Regression Baseline

```text
159 tests
159 passed
0 failed
```

**Build Status:** Stable

**Backend Status:** Functional and containerized

**API Documentation:** Available through Swagger UI

**Database:** PostgreSQL

**Test Database:** H2

## Engineering Highlights

This project demonstrates practical experience with:

- Designing layered Spring Boot applications
- Building RESTful APIs
- Implementing business rules beyond basic CRUD operations
- Designing relational data models with JPA
- Building stateless JWT authentication
- Implementing role-based API authorization
- Preventing reservation conflicts using date-range queries
- Designing dynamic filtering, pagination, and sorting
- Implementing centralized API error handling
- Building request tracing using correlation IDs and MDC
- Measuring HTTP request execution time
- Writing automated tests across multiple application layers
- Isolating test and production environments
- Externalizing application configuration
- Containerizing Java applications
- Orchestrating Spring Boot and PostgreSQL with Docker Compose

## Author

**Yogendra**

Software Developer focused on backend engineering, Java, Spring Boot, REST APIs, microservices, cloud technologies, and AI-powered applications.

## License

This project is intended for educational, portfolio, and demonstration purposes.