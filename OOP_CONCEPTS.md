# MediSlot — Object-Oriented Programming (OOP) Architecture & Academic Report

> **Course**: Object-Oriented Programming Using Java  
> **Project**: MediSlot — Hospital Appointment & Patient Care Coordination System  
> **Academic Purpose**: Comprehensive university documentation demonstrating how core Object-Oriented principles, software engineering patterns, and modern Java features are architected and implemented throughout the MediSlot codebase.

---

## Table of Contents
1. [Architectural Overview & OOP Philosophy](#1-architectural-overview--oop-philosophy)
2. [Pillar 1: Encapsulation & Data Hiding](#2-pillar-1-encapsulation--data-hiding)
3. [Pillar 2: Inheritance & Hierarchical Modeling](#3-pillar-2-inheritance--hierarchical-modeling)
4. [Pillar 3: Polymorphism (Static & Dynamic)](#4-pillar-3-polymorphism-static--dynamic)
5. [Pillar 4: Abstraction & Interface-Driven Design](#5-pillar-4-abstraction--interface-driven-design)
6. [Method Overloading & Constructor Design](#6-method-overloading--constructor-design)
7. [Custom Exception Architecture](#7-custom-exception-architecture)
8. [Collections Framework & Generics](#8-collections-framework--generics)
9. [Modern Java 17 Features & Time API](#9-modern-java-17-features--time-api)
10. [Academic Viva Defense Q&A](#10-academic-viva-defense-qa)

---

## 1. Architectural Overview & OOP Philosophy

MediSlot is designed around pure Object-Oriented principles to solve a real-world healthcare coordination challenge. Rather than treating classes as dumb data structs, MediSlot utilizes domain-driven object models where objects possess both **state** (private data members) and **behavior** (business methods and invariants).

```
                      ┌────────────────────────────────────────┐
                      │             <<abstract>>               │
                      │                 User                   │
                      ├────────────────────────────────────────┤
                      │ - userId: Long                         │
                      │ - username: String                     │
                      │ - fullName: String                     │
                      │ - email: String                        │
                      │ - role: Role                           │
                      ├────────────────────────────────────────┤
                      │ + getDashboardUrl(): String [Abstract] │
                      │ + getRoleDescription(): String [Abs]   │
                      └───────────────────▲────────────────────┘
                                          │
        ┌───────────────────┬─────────────┴───────┬───────────────────┐
        │                   │                     │                   │
┌───────┴────────┐  ┌───────┴────────┐   ┌────────┴────────┐  ┌───────┴────────┐
│     Admin      │  │     Doctor     │   │  Receptionist   │  │    Patient     │
├────────────────┤  ├────────────────┤   ├─────────────────┤  ├────────────────┤
│ - superAdmin   │  │ - doctorId     │   │ - deskLocation  │  │ - patientId    │
│                │  │ - fee: double  │   │ - shiftType     │  │ - patientCode  │
│                │  │ - spec: String │   │                 │  │ - dob: Date    │
└────────────────┘  └────────────────┘   └─────────────────┘  └────────────────┘
```

The system strictly enforces the **SOLID** principles:
- **Single Responsibility Principle (SRP)**: Repositories handle database persistence, Services enforce transactions and business logic, Controllers manage HTTP request/response lifecycles, and Models represent domain state.
- **Open/Closed Principle (OCP)**: The abstract `User` class is open for extension (e.g., adding a `Nurse` or `Pharmacist` role) without modifying core authentication or dashboard routing routines.
- **Liskov Substitution Principle (LSP)**: Any subclass of `User` (`Doctor`, `Patient`, `Receptionist`, `Admin`) can be substituted wherever a `User` reference is accepted.
- **Interface Segregation Principle (ISP)**: Focused repository interfaces (`PatientRepository`, `DoctorRepository`, `AppointmentRepository`) expose only methods relevant to that specific aggregate.
- **Dependency Inversion Principle (DIP)**: High-level services depend on abstract repository interfaces, not concrete JDBC implementations.

---

## 2. Pillar 1: Encapsulation & Data Hiding

### Definition
Encapsulation is the mechanism of bundling data (attributes) and methods that manipulate that data into a single unit (class), while restricting direct access to the internal components from external classes.

### Code Implementation in MediSlot

#### A. Private Fields with Accessor Methods
All domain model classes in `com.medislot.model` declare attributes with the `private` access modifier. State access and mutations are mediated through public getters and setters.

```java
// File: src/main/java/com/medislot/model/User.java
public abstract class User {
    private Long userId;
    private String username;
    private String passwordHash; // Sensitive credential protected
    private Role role;
    private String fullName;
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Encapsulated accessors
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    // ... remaining getters and setters
}
```

#### B. Behavioral Encapsulation & Derived Properties
In addition to basic getters and setters, entities encapsulate internal business logic and derived calculations rather than exposing raw data for external calculation:

```java
// File: src/main/java/com/medislot/model/Patient.java
public class Patient extends User {
    private Long patientId;
    private String patientCode;
    private LocalDate dateOfBirth;
    private String gender;
    // ...

    /**
     * Encapsulated domain logic: Age is dynamically computed from dateOfBirth
     * rather than stored as a redundant, drifting column in the database.
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
```

```java
// File: src/main/java/com/medislot/model/AppointmentSlot.java
public class AppointmentSlot {
    private Long slotId;
    private Long doctorId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;

    /**
     * Encapsulates state check logic within the domain entity.
     */
    public boolean isAvailable() {
        return SlotStatus.AVAILABLE.equals(this.status);
    }

    public boolean canBeBooked() {
        return isAvailable() && !isPastSlot();
    }

    private boolean isPastSlot() {
        if (slotDate.isBefore(LocalDate.now())) {
            return true;
        }
        return slotDate.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now());
    }
}
```

#### C. POJO Encapsulation
In `AppointmentSummaryPOJO.java`, encapsulation is demonstrated without framework dependencies:
```java
// File: src/main/java/com/medislot/model/AppointmentSummaryPOJO.java
public class AppointmentSummaryPOJO {
    private String reference;
    private String patientName;
    private String doctorName;
    private String department;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private double consultationFee;

    public String getFormattedSummary() {
        return String.format("[%s] %s with Dr. %s (%s) on %s at %s - Status: %s - Fee: $%.2f",
                reference, patientName, doctorName, department, appointmentDate, appointmentTime, status, consultationFee);
    }
}
```

---

## 3. Pillar 2: Inheritance & Hierarchical Modeling

### Definition
Inheritance is the mechanism by which one class acquires the properties and behaviors of another class. It promotes code reusability, establishes clean hierarchical relationships, and forms the bedrock for polymorphism.

### Code Implementation in MediSlot

#### A. Abstract Base Class (`User`)
The `User` class acts as the general parent class in the `is-a` relationship. It encapsulates fundamental attributes common to all human actors interacting with the hospital platform.

```java
// File: src/main/java/com/medislot/model/User.java
public abstract class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private Role role;
    private String fullName;
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Constructors
    public User() {}

    public User(Long userId, String username, String passwordHash, Role role,
                String fullName, String email, String phone, boolean isActive,
                LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    // ...
}
```

#### B. Concrete Specialized Subclasses
Specialized actors extend `User` and invoke the parent constructor using `super(...)`:

1. **`Doctor extends User`**:
   Adds clinical and hospital attributes:
   - `Long doctorId`
   - `Long departmentId`
   - `String departmentName`
   - `String specialization`
   - `String qualification`
   - `int experienceYears`
   - `double consultationFee`
   - `DoctorStatus status`

2. **`Patient extends User`**:
   Adds patient demographic and care coordination attributes:
   - `Long patientId`
   - `String patientCode`
   - `LocalDate dateOfBirth`
   - `String gender`
   - `String bloodGroup`
   - `String address`
   - `String emergencyContact`
   - `LocalDateTime registrationDate`

3. **`Receptionist extends User`**:
   Adds front desk administrative attributes:
   - `Long receptionistId`
   - `String deskLocation`
   - `String shiftType`

4. **`Admin extends User`**:
   Adds administrative authorization attributes:
   - `Long adminId`
   - `boolean superAdmin`

#### C. Reuse via `super` Constructor Chaining
```java
// File: src/main/java/com/medislot/model/Doctor.java
public class Doctor extends User {
    private Long doctorId;
    private Long departmentId;
    private String specialization;
    // ...

    public Doctor(Long userId, String username, String passwordHash, String fullName,
                  String email, String phone, boolean isActive, LocalDateTime createdAt,
                  Long doctorId, Long departmentId, String specialization,
                  String qualification, int experienceYears, double consultationFee,
                  DoctorStatus status) {
        // Explicit constructor chaining to superclass
        super(userId, username, passwordHash, Role.DOCTOR, fullName, email, phone, isActive, createdAt);
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.status = status;
    }
}
```

---

## 4. Pillar 3: Polymorphism (Static & Dynamic)

### Definition
Polymorphism ("many forms") allows a single interface or reference type to represent different underlying forms. Java supports both:
1. **Compile-Time (Static) Polymorphism**: Achieved via **Method Overloading**.
2. **Runtime (Dynamic) Polymorphism**: Achieved via **Method Overriding** through inheritance and interface implementation.

### A. Dynamic (Runtime) Polymorphism via Method Overriding

#### Abstract Method Contract in `User.java`:
```java
// File: src/main/java/com/medislot/model/User.java
public abstract class User {
    // ...
    public abstract String getDashboardUrl();
    public abstract String getRoleDescription();
}
```

#### Divergent Polymorphic Implementations:
Each subclass overrides these methods to provide role-specific routing and descriptions:

```java
// Admin.java
@Override
public String getDashboardUrl() {
    return "/admin/dashboard";
}
@Override
public String getRoleDescription() {
    return "Hospital Operations Executive & System Administrator";
}

// Doctor.java
@Override
public String getDashboardUrl() {
    return "/doctor/dashboard";
}
@Override
public String getRoleDescription() {
    return "Attending Medical Specialist & Clinical Consultant";
}

// Receptionist.java
@Override
public String getDashboardUrl() {
    return "/receptionist/dashboard";
}
@Override
public String getRoleDescription() {
    return "Front Desk Coordinator & Patient Intake Specialist";
}

// Patient.java
@Override
public String getDashboardUrl() {
    return "/patient/dashboard";
}
@Override
public String getRoleDescription() {
    return "Registered Care Recipient";
}
```

#### Polymorphic Invocation at Runtime:
During authentication or navigation, the system operates on a generic `User` reference without needing `instanceof` cascades or switch statements:
```java
User currentUser = securityService.getAuthenticatedUser();
// Dynamic dispatch invokes the subclass implementation at runtime:
String targetUrl = currentUser.getDashboardUrl();
return "redirect:" + targetUrl;
```

### B. Dynamic Polymorphism via Interface Implementation
The repository and service layers use interface-driven design:
```
           <<interface>>
        PatientRepository
                ▲
                │ implements
        ┌───────┴───────────────┐
        │                       │
JdbcPatientRepository   MockPatientRepository
  (Spring JdbcTemplate)       (Unit Testing)
```
The caller (`PatientServiceImpl`) holds a reference of type `PatientRepository`. The concrete bean (`JdbcPatientRepository`) is injected polymorphically by Spring at runtime.

### C. Static (Compile-Time) Polymorphism via Method Overloading
Method overloading allows multiple methods in the same class to share the same name with different parameter signatures.

In `TraditionalPatientJdbcRepository.java`:
```java
// Overload 1: Find by numerical primary key
public Optional<Patient> findById(Long patientId) { ... }

// Overload 2: Find by formatted alphanumeric hospital code
public Optional<Patient> findByPatientCode(String patientCode) { ... }

// Overload 3: Find by contact credential (email or phone)
public Optional<Patient> findByEmailOrPhone(String identifier) { ... }
```
The Java compiler resolves which method to invoke at compile time based strictly on argument types and call sites.

---

## 5. Pillar 4: Abstraction & Interface-Driven Design

### Definition
Abstraction is the concept of exposing only essential features and operations while hiding the internal implementation complexity. In Java, abstraction is achieved via **Abstract Classes** and **Interfaces**.

### Architecture in MediSlot

MediSlot decouples high-level business modules from low-level database operations:

```
┌─────────────────────────────────┐
│     PatientController (MVC)     │
└───────────────┬─────────────────┘
                │ depends on interface
┌───────────────▼─────────────────┐
│  <<interface>> PatientService   │
└───────────────▲─────────────────┘
                │ implements
┌───────────────┴─────────────────┐
│       PatientServiceImpl        │
└───────────────┬─────────────────┘
                │ depends on interface
┌───────────────▼─────────────────┐
│ <<interface>> PatientRepository │
└───────────────▲─────────────────┘
                │ implements
┌───────────────┴─────────────────┐
│      JdbcPatientRepository      │
└─────────────────────────────────┘
```

#### Code Sample:
```java
// Interface: Exposes clean contract without leaking SQL details
public interface PatientService {
    Patient registerPatient(PatientRegistrationDto dto);
    Optional<Patient> findById(Long patientId);
    Optional<Patient> findByUserId(Long userId);
    List<Patient> searchPatients(String query);
    Patient updatePatient(Long patientId, PatientRegistrationDto dto);
}
```
The controller has zero awareness of MySQL, SQL queries, table names, or connection pools. It simply interacts with `PatientService`.

---

## 6. Method Overloading & Constructor Design

### A. Overloaded Constructors
MediSlot domain models provide multiple constructors to support different usage contexts (e.g., ORM reflection vs. full initialization vs. unit testing):

```java
// File: src/main/java/com/medislot/model/AppointmentSlot.java
public class AppointmentSlot {
    // 1. Default No-Arg Constructor (required for serialization/mappers)
    public AppointmentSlot() {}

    // 2. Convenience Constructor for slot generation (omitting auto-generated slotId)
    public AppointmentSlot(Long doctorId, LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.AVAILABLE;
    }

    // 3. Full Canonical Constructor (for database RowMapper hydration)
    public AppointmentSlot(Long slotId, Long doctorId, LocalDate slotDate,
                           LocalTime startTime, LocalTime endTime, SlotStatus status) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
```

---

## 7. Custom Exception Architecture

### Checked vs. Unchecked Exceptions in Enterprise Java
- **Checked Exceptions** (`Exception`): Checked at compile time; require explicit `try-catch` or `throws` clauses. Often introduce excessive boilerplate in layered enterprise applications.
- **Unchecked Exceptions** (`RuntimeException`): Evaluated at runtime. Standard in modern Spring Boot applications because they integrate seamlessly with Spring's declarative transaction rollback mechanism (`@Transactional` automatically rolls back on unchecked exceptions by default).

### The MediSlot Exception Hierarchy

```
                      ┌──────────────────────────┐
                      │     java.lang.Object     │
                      └────────────▲─────────────┘
                                   │
                      ┌────────────┴─────────────┐
                      │        Throwable         │
                      └────────────▲─────────────┘
                                   │
                      ┌────────────┴─────────────┐
                      │        Exception         │
                      └────────────▲─────────────┘
                                   │
                      ┌────────────┴─────────────┐
                      │     RuntimeException     │
                      └────────────▲─────────────┘
                                   │
            ┌──────────────────────┴──────────────────────┐
            │                                             │
┌───────────┴──────────────┐                 ┌────────────┴────────────┐
│ ResourceNotFoundException│                 │    MediSlotException    │
└───────────▲──────────────┘                 └────────────▲────────────┘
            │                                             │
   ┌────────┴────────┬───────────────────┐    ┌───────────┴───────────┐
   │                 │                   │    │                       │
┌──┴──────────┐ ┌────┴─────────┐ ┌───────┴────┴───┐     ┌─────────────┴─────────────┐
│ PatientNot- │ │  DoctorNot-  │ │  Appointment-  │     │ DuplicateBookingException │
│ FoundExcp   │ │  FoundExcp   │ │  NotFoundExcp  │     ├───────────────────────────┤
└─────────────┘ └──────────────┘ └────────────────┘     │ SlotUnavailableException  │
                                                        ├───────────────────────────┤
                                                        │ InvalidAppointmentState-  │
                                                        │ Exception                 │
                                                        └───────────────────────────┘
```

### Code Implementation:
```java
// File: src/main/java/com/medislot/exception/ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// File: src/main/java/com/medislot/exception/PatientNotFoundException.java
public class PatientNotFoundException extends ResourceNotFoundException {
    public PatientNotFoundException(Long patientId) {
        super("Patient not found with ID: " + patientId);
    }
    public PatientNotFoundException(String patientCode) {
        super("Patient not found with code: " + patientCode);
    }
}

// File: src/main/java/com/medislot/exception/DuplicateBookingException.java
public class DuplicateBookingException extends RuntimeException {
    public DuplicateBookingException(String message) {
        super(message);
    }
}
```

### Global Exception Translation (`GlobalExceptionHandler.java`)
Using `@ControllerAdvice`, custom exceptions are caught globally and translated into user-friendly error banners and HTTP status codes:
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SlotUnavailableException.class)
    public String handleSlotUnavailable(SlotUnavailableException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/patient/book";
    }

    @ExceptionHandler(DuplicateBookingException.class)
    public String handleDuplicate(DuplicateBookingException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Duplicate Booking Conflict: " + ex.getMessage());
        return "redirect:/patient/book";
    }
}
```

---

## 8. Collections Framework & Generics

### Utilization across MediSlot

1. **Type-Safe Collections**:
   - `List<Doctor>`: Returned by `DoctorRepository.findAll()`.
   - `List<AppointmentSlot>`: Used to model daily schedule time tables.
   - `Map<DayOfWeek, List<DoctorSchedule>>`: Used in the doctor availability portal to group shift timings by day.
2. **Generics in Data Access**:
   - `Optional<T>`: Used extensively in repository return types (`Optional<Patient>`, `Optional<Doctor>`) to eliminate `NullPointerException` risks and force callers to handle absence explicitly (`orElseThrow(...)`).
   - `RowMapper<T>`: Spring's generic functional interface parameterized by domain model:
     ```java
     private final RowMapper<Patient> patientRowMapper = (rs, rowNum) -> {
         Patient p = new Patient();
         p.setPatientId(rs.getLong("patient_id"));
         // ...
         return p;
     };
     ```
3. **Java 8+ Stream API**:
   Used for functional operations such as filtering, mapping, and aggregating in-memory domain sets:
   ```java
   // Example: Finding active available slots for a specific date
   List<AppointmentSlot> availableSlots = slots.stream()
       .filter(AppointmentSlot::isAvailable)
       .sorted(Comparator.comparing(AppointmentSlot::getStartTime))
       .collect(Collectors.toList());
   ```

---

## 9. Modern Java 17 Features & Time API

MediSlot is compiled on OpenJDK 17 and takes full advantage of modern Java language improvements:

### A. The Java 8+ `java.time` API (JSR-310)
Legacy `java.util.Date` and `java.util.Calendar` are prone to concurrency issues, mutable state bugs, and zero-indexed months. MediSlot strictly uses modern immutable time classes:
- `LocalDate`: For birth dates, appointment dates (`slot_date`).
- `LocalTime`: For shift start and end times, slot boundaries (`09:00:00`, `09:30:00`).
- `LocalDateTime`: For exact audit logs, creation timestamps, and booking confirmation times.
- `Period` and `Duration`: For computing patient age (`Period.between(dob, now)`) and slot durations (`ChronoUnit.MINUTES`).

### B. Text Blocks (Java 15+)
Used throughout JDBC repositories for readable, multi-line SQL statements without messy string concatenation:
```java
String sql = """
    SELECT s.slot_id, s.doctor_id, s.slot_date, s.start_time, s.end_time, s.status
    FROM appointment_slots s
    WHERE s.doctor_id = ?
      AND s.slot_date = ?
      AND s.status = 'AVAILABLE'
    ORDER BY s.start_time ASC
    """;
```

### C. Switch Expressions
Used for clean status transitions and badge styling mappings without fall-through errors:
```java
String badgeClass = switch (status) {
    case BOOKED, CONFIRMED -> "badge-primary";
    case CHECKED_IN -> "badge-info";
    case COMPLETED -> "badge-success";
    case CANCELLED, NO_SHOW -> "badge-danger";
};
```

---

## 10. Academic Viva Defense Q&A

### Q1: Why did you create an abstract `User` class instead of an interface?
> **Answer**: An abstract class was chosen because all users share common **state** (`userId`, `username`, `passwordHash`, `email`, `phone`, `createdAt`) and common **implementation** logic (such as getter/setter validation). An interface cannot hold mutable instance state. Subclasses (`Doctor`, `Patient`, etc.) inherit this common state while being forced to provide concrete implementations for abstract behavior (`getDashboardUrl()` and `getRoleDescription()`).

### Q2: How does MediSlot demonstrate Polymorphism in its web routing?
> **Answer**: Upon authentication, the security interceptor extracts the authenticated `User` object. Rather than using procedural `if-else` or `switch` statements checking user roles, it calls `user.getDashboardUrl()`. At runtime, dynamic method dispatch invokes the subclass method (e.g., `Patient.getDashboardUrl()` returns `"/patient/dashboard"` while `Doctor.getDashboardUrl()` returns `"/doctor/dashboard"`).

### Q3: What is the difference between Method Overloading and Method Overriding in your project?
> **Answer**:
> - **Method Overloading** (Compile-time polymorphism) is demonstrated in `TraditionalPatientJdbcRepository` where `findById(Long)` and `findByPatientCode(String)` share the same method name but have different parameter types.
> - **Method Overriding** (Runtime polymorphism) is demonstrated in `Doctor.java` and `Patient.java` overriding the abstract methods `getDashboardUrl()` and `getRoleDescription()` defined in `User.java`.

### Q4: Why did you choose unchecked custom exceptions over checked exceptions?
> **Answer**: In modern enterprise Spring architectures, checked exceptions cause unnecessary boilerplate code across controllers and services. By extending `RuntimeException`, exceptions such as `SlotUnavailableException` and `DuplicateBookingException` bubble up cleanly to the `@ControllerAdvice` handler and trigger Spring's declarative `@Transactional` rollback mechanism automatically without requiring cluttered `throws` declarations on every method signature.

### Q5: How is Encapsulation maintained when retrieving entities from the database?
> **Answer**: In both the traditional JDBC repository and Spring JdbcTemplate `RowMapper`, database records are read column-by-column and mapped via public setters or canonical constructors into private fields. Raw database `ResultSet` cursors are never leaked outside the data access layer.

---

*This document forms part of the official submission portfolio for MediSlot.*
