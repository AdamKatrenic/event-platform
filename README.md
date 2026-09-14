# Event Platform - Spring Boot 3 Application

A comprehensive event management platform built with **Spring Boot 3**, **Spring Security**, and **JWT authentication**. The application provides user management, event creation, booking capabilities, and administrative functions.

---

## Features

### Authentication and Authorization
- JWT-based token authentication
- Secure user login/logout functionality
- Role-based access control (USER and ADMIN roles)

### Event Management
- Create, read, update, and delete events
- Detailed event information retrieval
- Search events by title

### Booking System
- Event booking functionality for users
- Booking management and tracking

### User Management  
- User registration and login
- Profile view capabilities

### Admin Dashboard
- Administrative endpoints for platform management
- Full event management capabilities (all CRUD operations)
- User administration functions

---

## Technology Stack

- **Spring Boot 3.x** with Java 17/21
- **Spring Security** with JWT authentication
- **Hibernate/JPA** for database persistence
- **HikariCP** connection pooling
- **Lombok** for reduced boilerplate code
- **JUnit 5** for testing

---

## Project Structure

```
src/main/java/com/adam/event_platform/
├── AppConfig.java              # Bean definitions and configuration
├── controller/
│   ├── AdminController.java    # Admin endpoints
│   ├── AuthController.java     # Authentication endpoints
│   └── UserController.java     # User management endpoints
├── entity/                     # JPA entities (User, Event, Booking)
├── exception/                  # Custom exceptions and GlobalExceptionHandler
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
| GET | /api/user/{id} | Get user by ID | No |
| POST | /api/user | Register new user | No |

### Admin

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| All | /api/admin/** | Admin endpoints | Yes (ADMIN role) |

---

## Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Database (H2 in-memory by default, or configure your own)

### Build and Run

```bash
# Build the application
mvn clean install

# Run with Maven
mvn spring-boot:run

# Or use the jar file
java -jar target/event-platform.jar
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

The application can be configured via `application.properties`:

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

# JWT Configuration
jwt.secret=my-secret-key-for-jwt-signing-minimum-32-characters
jwt.expiration=86400000

# Security (configure as needed)
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

## License

This project is for educational and demonstration purposes.
