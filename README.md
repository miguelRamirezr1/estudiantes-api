# Student Management REST API

A lightweight RESTful API for managing student records, built with Spring Boot following industry best practices and clean architecture principles.

## Table of Contents

- [Overview](#overview)
- [Architecture & Design Decisions](#architecture--design-decisions)
- [Technologies & Dependencies](#technologies--dependencies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Testing with Postman](#testing-with-postman)
- [How It Works: Behind the Scenes](#how-it-works-behind-the-scenes)

---

## Overview

This project implements a simple but complete RESTful API for a student record system. It demonstrates proper separation of concerns using the MVC (Model-View-Controller) architecture pattern with an additional Service layer for business logic.

**Key Features:**
- Student registration with unique ID validation
- Retrieve all registered students
- In-memory data persistence using ConcurrentHashMap
- Comprehensive input validation
- Global exception handling with meaningful error responses
- Thread-safe concurrent request handling

---

## Architecture & Design Decisions

### Why a Service Layer Between Controller and Repository?

The Service layer acts as a middle tier that handles **business logic**, providing clear separation of concerns:

**Without Service Layer:**
```java
// Controller has to handle both HTTP and business logic
@PostMapping("/estudiantes")
public ResponseEntity<?> create(@RequestBody Estudiante estudiante) {
    if (repository.exists(estudiante.getId())) {  // Business logic in controller
        throw new DuplicateIdException();
    }
    repository.save(estudiante);
    return ResponseEntity.status(201).body(estudiante);
}
```

**With Service Layer:**
```java
// Controller only handles HTTP concerns
@PostMapping("/estudiantes")
public ResponseEntity<?> create(@RequestBody Estudiante estudiante) {
    Estudiante saved = service.registerStudent(estudiante);
    return ResponseEntity.status(201).body(saved);
}

// Service handles business logic
public Estudiante registerStudent(Estudiante estudiante) {
    if (repository.exists(estudiante.getId())) {
        throw new IllegalArgumentException("Duplicate ID");
    }
    return repository.save(estudiante);
}
```

**Benefits of this separation:**
- **Controller** = HTTP concerns only (request/response, status codes, routing)
- **Service** = Business logic (validation rules, workflow orchestration)
- **Repository** = Data access only (CRUD operations on storage)
- Easier to test each layer independently
- Business logic can be reused across different controllers or interfaces
- Changes to business rules don't affect HTTP handling

### Why There's No "View" in This REST API Backend

In traditional web applications using MVC, the View renders HTML pages for browsers. However, this is a **pure REST API backend** that returns JSON data, not HTML pages.

**Traditional MVC with Views:**
```
Controller → Model → View (HTML templates like JSP/Thymeleaf)
```
The View renders the data into HTML for human consumption in a browser.

**REST API Architecture (This Project):**
```
Controller → Model → JSON Response
```
**The JSON itself IS the "View"** - it's the representation of your data.

**Where does the "View" happen in a REST API?**
- In **Postman** - when you make a request and see the JSON response displayed
- In a **separate frontend application** (React, Angular, Vue) that consumes this API
- In any **HTTP client** that makes requests and displays the responses

The backend API provides the data; the client (frontend/tool) decides how to display it. This separation allows multiple different frontends (web app, mobile app, desktop app) to consume the same API.

### Why the Controller Only Handles HTTP Requests and Responses

The Controller's sole responsibility is to act as the HTTP entry point:

**Controller Responsibilities (HTTP Layer):**
- Map HTTP routes (POST /estudiantes, GET /estudiantes)
- Parse incoming requests and extract data
- Validate request format (using @Valid annotation)
- Set appropriate HTTP status codes (201, 200, 409, 400)
- Convert Java objects to JSON responses

**What the Controller Does NOT Do:**
- Business logic (duplicate checking, complex validations)
- Data storage operations
- Complex calculations or transformations

By delegating business logic to the Service layer, we achieve:
- **Single Responsibility Principle** - each class has one clear job
- **Reusability** - service methods can be called from multiple controllers
- **Testability** - HTTP logic and business logic can be tested separately
- **Maintainability** - changes to business rules don't require modifying HTTP handling

### Purpose of Global Exception Handling

The `@ControllerAdvice` class with `@ExceptionHandler` methods provides centralized error handling across the entire application.

**Without Global Exception Handling:**
```java
// Every controller method needs try-catch blocks
@PostMapping("/estudiantes")
public ResponseEntity<?> create(@RequestBody Estudiante estudiante) {
    try {
        Estudiante saved = service.registerStudent(estudiante);
        return ResponseEntity.status(201).body(saved);
    } catch (IllegalArgumentException ex) {
        return ResponseEntity.status(409).body(createErrorResponse(ex));
    } catch (ValidationException ex) {
        return ResponseEntity.status(400).body(createErrorResponse(ex));
    }
    // Repetitive error handling in every method
}
```

**With Global Exception Handling:**
```java
// Controller stays clean - just happy path
@PostMapping("/estudiantes")
public ResponseEntity<?> create(@RequestBody Estudiante estudiante) {
    Estudiante saved = service.registerStudent(estudiante);
    return ResponseEntity.status(201).body(saved);
}

// GlobalExceptionHandler intercepts all exceptions
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<?> handleDuplicate(IllegalArgumentException ex) {
    return ResponseEntity.status(409).body(createErrorResponse(ex));
}
```

**Benefits:**
- **DRY Principle** - error handling logic written once, applied everywhere
- **Consistent error responses** - all errors follow the same JSON structure
- **Cleaner controllers** - focus on the happy path, not error scenarios
- **Centralized monitoring** - one place to add logging or alerting for errors
- **Easy customization** - change error format across entire API by modifying one class

---

## Technologies & Dependencies

### Project Settings

- **Build Tool:** Gradle
- **Java Version:** 21 (LTS - Long Term Support)
- **Spring Boot Version:** 3.2.x / 3.3.x (latest stable)
- **Packaging:** JAR
- **Server Port:** 8080 (configurable in `application.properties`)

### Dependencies Added to Spring Boot

```gradle
dependencies {
    // Core REST API functionality
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Bean validation (enables @Valid, @NotNull, @NotBlank annotations)
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Auto-reload during development (no need to restart server for code changes)
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Reduces boilerplate code (auto-generates getters, setters, constructors)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing support
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**Why Each Dependency:**

- **spring-boot-starter-web**: Provides embedded Tomcat server, Spring MVC for REST endpoints, Jackson for JSON serialization/deserialization
- **spring-boot-starter-validation**: Enables declarative validation using annotations like `@NotNull`, `@NotBlank` on model fields
- **spring-boot-devtools**: Monitors code changes and automatically restarts the application during development for faster iteration
- **lombok**: Annotation processor that generates repetitive code (getters, setters, constructors, toString, etc.) at compile time, reducing boilerplate
- **spring-boot-starter-test**: Includes JUnit, Mockito, and Spring Test for unit and integration testing

---

## Project Structure

```
estudiantes-api/
├── src/
│   ├── main/
│   │   ├── java/com/devops/estudiantes/
│   │   │   ├── EstudiantesApiApplication.java    # Main entry point
│   │   │   ├── model/
│   │   │   │   └── Estudiante.java               # Student data model
│   │   │   ├── repository/
│   │   │   │   └── EstudianteRepository.java     # In-memory data storage
│   │   │   ├── service/
│   │   │   │   └── EstudianteService.java        # Business logic layer
│   │   │   ├── controller/
│   │   │   │   └── EstudianteController.java     # HTTP endpoints
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java   # Centralized error handling
│   │   └── resources/
│   │       └── application.properties             # Configuration
│   └── test/
│       └── java/                                  # Test files
├── build.gradle                                   # Gradle build configuration
└── README.md
```

**Architecture Flow:**
```
HTTP Request → Controller → Service → Repository → In-Memory Storage (ConcurrentHashMap)
                    ↓           ↓           ↓
                 HTTP        Business    Data
                 Layer       Logic       Access
```

---

## Getting Started

### Prerequisites

- **Java 21** (or compatible version)
- **Gradle** (or use the included Gradle wrapper)
- **Git** (to clone the repository)
- **Postman** (recommended for API testing)

### Clone the Repository

```bash
git clone https://github.com/yourusername/estudiantes-api.git
cd estudiantes-api
```

### Install Dependencies

Gradle will automatically download all dependencies when you build the project. You don't need to install anything manually.

**Using Gradle Wrapper (recommended):**

On Mac/Linux:
```bash
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

The first run will download Gradle and all project dependencies. This may take a few minutes.

### Run the Application

**Option 1: Using Gradle**

Mac/Linux:
```bash
./gradlew bootRun
```

Windows:
```bash
gradlew.bat bootRun
```

**Option 2: Using your IDE**

1. Open the project in IntelliJ IDEA, Eclipse, or VS Code
2. Navigate to `EstudiantesApiApplication.java`
3. Right-click and select "Run EstudiantesApiApplication"

**Verify the Server is Running:**

You should see output ending with:
```
Tomcat started on port(s): 8080 (http)
Started EstudiantesApiApplication in X.XXX seconds
```

The API is now accessible at `http://localhost:8080`

---

## API Endpoints

### Base URL
```
http://localhost:8080
```

### Endpoints

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/estudiantes` | Register a new student | 201 Created, 409 Conflict, 400 Bad Request |
| GET | `/estudiantes` | Retrieve all students | 200 OK |

### Data Model

**Estudiante (Student) JSON Structure:**

```json
{
  "id": "string (required, unique)",
  "nombre": "string (required, not blank)",
  "carrera": "string (required, not blank)"
}
```

**Example:**
```json
{
  "id": "000125354",
  "nombre": "Fito Paez",
  "carrera": "Ingeniería de Sonido"
}
```

---

## Testing with Postman

### Test 1: Register a Student (Success Case)

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "id": "000125354",
  "nombre": "Fito Paez",
  "carrera": "Ingeniería de Sonido"
}
```

**Expected Response:**
- **Status:** 201 Created
- **Body:**
```json
{
  "id": "000125354",
  "nombre": "Fito Paez",
  "carrera": "Ingeniería de Sonido"
}
```

---

### Test 2: Register Another Student

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "id": "000125355",
  "nombre": "Gustavo Cerati",
  "carrera": "Ingeniería de Sistemas"
}
```

**Expected Response:**
- **Status:** 201 Created
- **Body:** The student data you sent

---

### Test 3: Attempt to Register Duplicate ID (Error Case)

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "id": "000125354",
  "nombre": "Another Student",
  "carrera": "Another Program"
}
```

**Expected Response:**
- **Status:** 409 Conflict
- **Body:**
```json
{
  "timestamp": "2026-02-12T...",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un estudiante con el ID: 000125354"
}
```

This confirms duplicate ID validation is working correctly.

---

### Test 4: Send Invalid Data (Missing Required Fields)

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "id": "123"
}
```

**Expected Response:**
- **Status:** 400 Bad Request
- **Body:**
```json
{
  "timestamp": "2026-02-12T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "validationErrors": {
    "nombre": "El nombre no puede ser nulo",
    "carrera": "La carrera no puede ser nula"
  }
}
```

This confirms input validation is working correctly.

---

### Test 5: Send Blank/Empty Fields

**Request:**
- **Method:** POST
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**

```json
{
  "id": "   ",
  "nombre": "",
  "carrera": "   "
}
```

**Expected Response:**
- **Status:** 400 Bad Request
- **Body:** Validation errors indicating fields cannot be blank

---

### Test 6: Retrieve All Students

**Request:**
- **Method:** GET
- **URL:** `http://localhost:8080/estudiantes`
- **Headers:** None required
- **Body:** None

**Expected Response:**
- **Status:** 200 OK
- **Body:**
```json
[
  {
    "id": "000125354",
    "nombre": "Fito Paez",
    "carrera": "Ingeniería de Sonido"
  },
  {
    "id": "000125355",
    "nombre": "Gustavo Cerati",
    "carrera": "Ingeniería de Sistemas"
  }
]
```

If no students have been registered yet, you'll receive an empty array: `[]`

---

## How It Works: Behind the Scenes

When you send that first POST request, here's the journey your data takes:

The JSON hits your `EstudianteController` at the POST endpoint. Spring's Jackson library deserializes the JSON into an `Estudiante` object. The `@Valid` annotation triggers validation, checking that all required fields are present and not blank. If validation passes, the controller calls `service.registerStudent()`. The service checks `repository.existsById()` to see if the ID is already taken. If it's unique, the repository stores it in the `ConcurrentHashMap` using the ID as the key. The saved student travels back up through the service to the controller, which wraps it in a `ResponseEntity` with status 201 and returns it. Spring converts the object back to JSON and sends the HTTP response to Postman.

If anything goes wrong along the way, like a duplicate ID or validation failure, the appropriate exception is thrown and caught by your `GlobalExceptionHandler`, which converts it to a proper error response.

**Visual Flow:**

```
1. Postman sends POST request with JSON
         ↓
2. EstudianteController receives request
         ↓
3. @Valid annotation validates the Estudiante object
         ↓ (if valid)
4. Controller calls service.registerStudent(estudiante)
         ↓
5. EstudianteService checks repository.existsById(id)
         ↓ (if unique)
6. Service calls repository.save(estudiante)
         ↓
7. EstudianteRepository stores in ConcurrentHashMap
         ↓
8. Saved student returns up: Repository → Service → Controller
         ↓
9. Controller wraps in ResponseEntity with status 201
         ↓
10. Spring converts to JSON and sends HTTP response
         ↓
11. Postman receives 201 Created with student data
```

**If an error occurs:**
```
Exception thrown anywhere in the chain
         ↓
GlobalExceptionHandler intercepts
         ↓
Converts to appropriate HTTP error response
         ↓
Postman receives error (409, 400, 500, etc.)
```

---

## Key Implementation Details

### Thread Safety

The application uses `ConcurrentHashMap` for in-memory storage, which is thread-safe and allows multiple simultaneous requests without data corruption. Spring Boot's embedded Tomcat server handles each HTTP request in a separate thread, making concurrency handling crucial.

### Data Persistence

Data is stored in-memory only. When the server restarts, all data is lost. This is by design for this educational project. To add persistence, you could integrate Spring Data JPA with a database like PostgreSQL or MySQL.

### Validation Strategy

The project uses two layers of validation:
1. **Format validation** - `@Valid` with Bean Validation annotations (`@NotNull`, `@NotBlank`)
2. **Business rule validation** - Service layer checks (duplicate ID prevention)

This separation ensures clean code and proper error responses at the appropriate abstraction level.