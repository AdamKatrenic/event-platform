# Event Platform - Spring Boot 3 Application

A comprehensive event management platform built with **Spring Boot 3**, **Spring Security**, and **JWT authentication**. The application provides user management, event creation, booking capabilities, and administrative functions. It integrates with Apache Kafka for real-time event streaming and notification delivery.

---

## Shipped Projects

| Project | Stack | Status |
|---------|-------|--------|
| Banking Platform | Java, Spring Boot, MySQL, JWT, Next.js, Docker | Live |
| Event Platform | Java, Spring Boot, PostgreSQL, Kafka, JWT, Maven | Development |
| Proofs — roofing company | Next.js, TypeScript, Tailwind, Sanity | Live |
| David Pillar — event videographer | Next.js, TypeScript, Tailwind, Sanity | Live |

---

## Technology Stack

**Backend**

Java 21 · Spring Boot 3.2.5 · Spring Data JPA · PostgreSQL · MySQL · JWT · Apache Kafka · Docker · Maven · JUnit 5 · Mockito

**Frontend**

Next.js · TypeScript · Tailwind CSS · Sanity CMS · React · JavaScript · Chart.js · Framer Motion

**Tools**

Git · GitHub · Railway · Vercel · IntelliJ IDEA · VS Code · Postman · Docker · Local AI Models (LLMs) · Qwen3.5

---

## Features

### Authentication and Authorization
- JWT-based token authentication using JsonWebTokens
- Secure user login/logout functionality
- Role-based access control (USER and ADMIN roles)
- Spring Security with custom JwtAuthenticationFilter

### Event Management
- Create, read, update, and delete events
- Detailed event information retrieval
- Search events by title
- Kafka integration for real-time event publishing

### Booking System
- Event booking functionality for users
- Booking management and tracking
- Capacity validation to prevent overbooking
- Transaction-safe operations

### User Management  
- User registration and login
- Profile view capabilities
- Admin user management endpoints

### Admin Dashboard
- Administrative endpoints for platform management
- Full event management capabilities (all CRUD operations)
- User administration functions

---

## Kafka Integration

The application uses Apache Kafka for decoupled microservices communication and real-time notification delivery. KafkaConfig provides a ProducerFactory bean for asynchronous event publishing.

**Kafka Topics:**

- **bookings**: Used for broadcasting booking events (confirmed, cancelled) to interested services or subscribers.

**Implementation Details:**

- Auto-configured via Spring Boot's @EnableKafka
- Custom KafkaConfig with DefaultKafkaProducerFactory for explicit control
- Asynchronous message sending using CompletableFuture
- StringSerializer for both keys and values (JSON-based payload)

---

## Project Structure

```
src/main/java/com/adam/event_platform/
├── AppConfig.java              # Bean definitions and configuration
├── config/                     # Kafka, Security configurations
│   ├── KafkaConfig.java       # Kafka producer beans
│   └── SecurityConfig.java    # Spring Security setup
├── controller/
│   ├── AdminController.java    # Admin endpoints
│   ├── AuthController.java     # Authentication endpoints
│   └── UserController.java     # User management endpoints
├── dto/                        # Data Transfer Objects
├── entity/                     # JPA entities (User, Event, Booking)
├── exception/                  # Custom exceptions and GlobalExceptionHandler
├── model/                      # Domain models
├── repository/                 # JPA repositories
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   └── SecurityConfig.java
└── service/                    # Business logic services

src/test/java/com/adam/event_platform/
├── controller/
│   ├── AdminControllerTest.java
│   ├── AuthControllerTest.java
│   └── UserControllerTest.java
└── integration/                # Integration tests
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| POST | /api/auth/login | User login | No |
| POST | /api/auth/logout | User logout | Yes |
| POST | /api/auth/register | Register new user | No |

### Events

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | /api/event/{id} | Get event by ID | No |
| GET | /api/event/{title} | Get event by title | No |
| POST | /api/event | Create new event | Yes |
| PUT | /api/event/{title} | Update event | Yes |
| DELETE | /api/event/{title} | Delete event | Yes |

### Users

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | /api/v1/users/me | Get current user profile | Yes |
| POST | /api/user | Register new user | No |

### Admin

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| All | /api/v1/admin/** | Admin endpoints | Yes (ADMIN role) |

---

## Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Database (H2 in-memory by default, or configure PostgreSQL/MySQL)

### Build and Run

```bash
# Build the application with tests
mvn clean test

# Run with Maven
mvn spring-boot:run

# Or use the jar file
java -jar target/event-platform-0.0.1-SNAPSHOT.jar
```

### Kafka Setup (Optional)

To enable Kafka integration for event streaming:

```bash
# Start Kafka and ZooKeeper
cd docker
docker-compose up -d

# The application will automatically connect to localhost:9092
```

### Test the API

```bash
# Start the app first, then run these tests:

# Login to get a token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Create an event (requires authentication)
curl -X POST http://localhost:8080/api/event \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"title":"Conference","description":"Annual tech conference","start_date":"2024-12-01"}'
```

---

## Configuration

The application can be configured via `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080

# Database (H2 in-memory by default)
spring.datasource.url=jdbc:h2:mem:eventplatform
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=admin
spring.datasource.password=admin

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Kafka configuration (optional)
spring.kafka.bootstrap-server=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

---

## Testing

All tests are passing (16/16):

```bash
# Run all tests
mvn test

# Run specific test classes
mvn test -Dtest=AdminControllerTest,AuthControllerTest,UserControllerTest
```

### Test Results Summary

| Test Class | Tests | Status |
|------------|-------|--------|
| AdminControllerTest | 7 | Pass |
| AuthControllerTest | 6 | Pass |
| UserControllerTest | 3 | Pass |
| **Total** | **16** | **All Passing** |

---

## Global Exception Handling

The application uses a centralized `GlobalExceptionHandler` to handle common exceptions:

- `ResourceNotFoundException` - 404 Not Found
- `BadCredentialsException` - 401 Unauthorized
- `MethodArgumentNotValidException` - 400 Bad Request
- `AccessDeniedException` - 403 Forbidden
- Custom business exceptions (InsufficientCapacityException, UserAlreadyExistsException, InvalidBookingStateException)

---

## Currently Learning

Spring Kafka · Microservices · Angular · Kafka event streaming · Real-time notifications

---

## License

This project is for educational and demonstration purposes.
