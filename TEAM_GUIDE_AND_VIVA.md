# MediSlot — Complete Project Master Guide & Viva Defense Portfolio

> **For All Team Members & Faculty Evaluators**  
> **Course**: Object-Oriented Programming Using Java (Full-Stack Case Study)  
> **Project**: MediSlot — Hospital Appointment & Patient Care Coordination System  
> **Repository**: [https://github.com/krishmunjal01/MediSlot](https://github.com/krishmunjal01/MediSlot)  
> **Live Demo URL**: `https://c61d-117-253-208-84.ngrok-free.app` *(or `http://localhost:8080` locally)*

---

## Table of Contents
1. [Team Quick-Start & 60-Second Elevator Pitch](#1-team-quick-start--60-second-elevator-pitch)
2. [Demo Credentials & User Roles](#2-demo-credentials--user-roles)
3. [Technology Stack & Architectural Layers](#3-technology-stack--architectural-layers)
4. [Step-by-Step Viva Demonstration Script (Follow This in Front of Sir)](#4-step-by-step-viva-demonstration-script-follow-this-in-front-of-sir)
5. [The Core Technical Masteries (What Sir Will Scrutinize)](#5-the-core-technical-masteries-what-sir-will-scrutinize)
   - [5.1 Duplicate Booking Prevention & Pessimistic Locking](#51-duplicate-booking-prevention--pessimistic-locking)
   - [5.2 Traditional JDBC vs. Spring JdbcTemplate](#52-traditional-jdbc-vs-spring-jdbctemplate)
   - [5.3 Plain Old Java Object (POJO) vs. Spring-Managed Bean](#53-plain-old-java-object-pojo-vs-spring-managed-bean)
   - [5.4 The 4 Pillars of OOP in MediSlot](#54-the-4-pillars-of-oop-in-medislot)
6. [Database Schema & Entity Relationships](#6-database-schema--entity-relationships)
7. [Codebase Map: Where Everything Lives](#7-codebase-map-where-everything-lives)
8. [Automated Test Suite & Concurrency Verification](#8-automated-test-suite--concurrency-verification)
9. [The Ultimate Viva Defense Cheat Sheet (25+ Q&As)](#9-the-ultimate-viva-defense-cheat-sheet-25-qas)

---

## 1. Team Quick-Start & 60-Second Elevator Pitch

### What is MediSlot?
**MediSlot** is a full-stack hospital operations and patient care coordination platform. It solves the critical real-world problem of hospital bottlenecking: coordinating schedules across medical departments, enabling patients to book and reschedule consultations, empowering receptionists to manage arrivals and standby queues, and giving attending physicians a clinical documentation workspace.

### The 60-Second Pitch for Your Faculty Evaluator:
> *"Sir, MediSlot is not a superficial UI prototype or a generic CRUD project. It is an enterprise-grade hospital coordination platform built on Java 17, Spring Boot 3.2, and MySQL 8.0.*  
> *It features four dedicated role workflows—Administrator, Attending Doctor, Reception Desk, and Patient.*  
> *Architecturally, it fulfills every requirement of our OOP using Java case study:*  
> *1. It prevents duplicate bookings under concurrent load using pessimistic database row-level locking (`SELECT FOR UPDATE`) combined with database-level unique constraints.*  
> *2. It implements data persistence in two distinct ways side-by-side: raw Traditional JDBC using explicit Connections and PreparedStatements, and modern Spring JdbcTemplate using RowMappers.*  
> *3. It demonstrates runtime polymorphism and inheritance across our User hierarchy, custom runtime exception hierarchies, and a live side-by-side comparison of unmanaged POJOs versus Spring-managed singleton Beans with transaction proxies.*  
> *4. All analytics, metrics, and availability slots are driven by real SQL aggregate queries, wrapped in an understated, responsive healthcare design."*

---

## 2. Demo Credentials & User Roles

Every teammate should memorize these 4 pre-seeded demo accounts.  
*(Pro-Tip: On the login page, you can simply click the quick-fill buttons at the bottom instead of typing!)*

| Role | Username | Password | Full Name / Persona | Key Responsibilities to Demonstrate |
|---|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Marcus Aurelius | Operations dashboard, doctor/department management, slot generator, live JDBC & POJO demo pages |
| **Attending Doctor** | `dr.arjun` | `doctor123` | Dr. Arjun Mehta (Cardiology) | Today's patient queue, consultation documentation (diagnosis + Rx), completes visits |
| **Receptionist** | `receptionist` | `recep123` | Clara Oswald | Front desk 1-click arrival check-in, walk-in registration, rescheduling, waiting list |
| **Patient** | `eleanor` | `patient123` | Eleanor Vance | Step-by-step booking wizard, live slot selection, printable voucher, self-service cancel |

---

## 3. Technology Stack & Architectural Layers

MediSlot follows the classic, clean **Multi-Tier MVC Architecture**:

```
┌────────────────────────────────────────────────────────┐
│     Presentation Layer (Thymeleaf 3 + Semantic HTML5)   │
│   (Custom Editorial Design System, Mobile Drawer, JS)  │
└───────────────────────────▲────────────────────────────┘
                            │ HTTP GET / POST
┌───────────────────────────┴────────────────────────────┐
│               Spring MVC Web Controllers               │
│ (Auth, Admin, Doctor, Patient, Receptionist, API)      │
└───────────────────────────▲────────────────────────────┘
                            │ DTOs / Method Calls
┌───────────────────────────┴────────────────────────────┐
│            Transactional Business Service Layer        │
│   (Pessimistic Locks, Status Transitions, Validation)  │
└───────────────────────────▲────────────────────────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
┌────────────┴─────────────┐ ┌─────────────┴────────────┐
│   Spring JdbcTemplate    │ │      Traditional JDBC     │
│       Repositories       │ │  (Connection, Prepared-  │
│ (RowMapper<T>, HikariCP) │ │   Statement, ResultSet)  │
└────────────┬─────────────┘ └─────────────┬────────────┘
             │                             │
             └──────────────┬──────────────┘
                            │ Raw SQL Queries / Locks
┌───────────────────────────▼────────────────────────────┐
│                 MySQL 8.0 Relational DB                │
│ (ACID Transactions, Row Locks, Foreign Key Integrity)  │
└────────────────────────────────────────────────────────┘
```

- **Core Backend**: Java 17 LTS (OpenJDK 17).
- **Enterprise Framework**: Spring Boot 3.2.5.
- **Web MVC**: Spring MVC with Thymeleaf 3 server-rendered templates.
- **Security**: Spring Security 6.2 with BCrypt password hashing and role-based URL guards (`/admin/**`, `/doctor/**`, `/receptionist/**`, `/patient/**`).
- **Database**: MySQL 8.0 Relational Database.
- **Connection Pool**: HikariCP (high-performance JDBC connection pooling).
- **Data Persistence**: Dual implementation:
  - Spring `JdbcTemplate` across 10 repositories.
  - Raw Traditional `java.sql.*` in `TraditionalPatientJdbcRepository`.
- **Testing**: JUnit 5 Jupiter, `CountDownLatch` multithreaded concurrency tests, MockMvc.
- **Build System**: Apache Maven 3.9.

---

## 4. Step-by-Step Viva Demonstration Script (Follow This in Front of Sir)

If Sir asks: *"Show me the project and walk me through the features,"* follow this exact 5-step sequence:

### Step 1: Login as Administrator (`admin` / `admin123`)
1. Open the browser to the live link or `http://localhost:8080/login`.
2. Click the quick-fill button **"Admin (Marcus)"** and submit.
3. Show the **Executive Operations Dashboard**: Explain that all four metrics (*Appointments Today*, *Upcoming Bookings*, *Active Doctors*, *Registered Patients*) are calculated dynamically using SQL queries (`COUNT`, `GROUP BY`), not hardcoded numbers.
4. In the sidebar, navigate to **Technical Demonstration** (`/admin/technical-demonstration`):
   - Enter Patient ID `1` and click **"Execute Side-by-Side Comparison"**.
   - Show Sir how Traditional JDBC and Spring JdbcTemplate execute the exact same query, extract data, and display their execution latencies in milliseconds.
5. In the sidebar, navigate to **OOP Demonstration** (`/admin/oop-demonstration`):
   - Show Sir the live inspector contrasting the unmanaged `AppointmentSummaryPOJO` with the Spring-managed singleton `AppointmentServiceImpl` Bean.

### Step 2: Login as Patient (`eleanor` / `patient123`)
1. Click Sign Out, then click quick-fill **"Patient (Eleanor)"**.
2. Show the **My Health Home** dashboard: Eleanor's upcoming appointments and previous clinical consultation history.
3. Click **"Book New Appointment"**:
   - Select Department: *Cardiology*.
   - Select Doctor: *Dr. Arjun Mehta*.
   - Select a Date in the calendar.
   - **Show the Live Slots**: Notice that available consultation slots (`10:00`, `10:30`, etc.) load asynchronously without refreshing the page!
   - Select a slot, enter the reason for visit (*"Routine heart checkup"*), and click **"Confirm Consultation Booking"**.
4. Show the **Official Confirmation Slip**: Point out the unique alphanumeric reference code (e.g., `APT-20261123-XXXXX`), date, time, and consultation fee.

### Step 3: Demonstrate Concurrency & Double-Booking Prevention
1. Tell Sir: *"Sir, what happens if another patient tries to book that exact same doctor and time slot?"*
2. Open an Incognito window or another browser tab, log in as patient **Marcus** (`marcus` / `patient123`).
3. Attempt to book Dr. Arjun Mehta on the exact same date and time slot.
4. **Result**: The system rejects the booking and displays a clear alert: *"This slot is already booked or unavailable."*
5. Tell Sir: *"We also have an automated multithreaded test (`ConcurrencyBookingTest.java`) that simulates two threads hitting the database at the exact same millisecond using `CountDownLatch`, and MySQL pessimistic locking ensures only one succeeds."*

### Step 4: Login as Receptionist (`receptionist` / `recep123`)
1. Sign in as Receptionist **Clara**.
2. Show the **Reception Desk Dashboard**: Point out the live arrival queue.
3. Click **"Check-In Patient"**: Watch Eleanor's status transition immediately from `BOOKED` to `CHECKED_IN`.
4. Demonstrate **Rescheduling & Slot Release**:
   - Reschedule an appointment to a new slot.
   - Point out that our transaction automatically **releases the old slot back to `AVAILABLE`** so other patients can book it, while claiming the new slot as `BOOKED`.

### Step 5: Login as Doctor (`dr.arjun` / `doctor123`)
1. Sign in as Attending Physician **Dr. Arjun Mehta**.
2. Show the **Doctor Daily Schedule**: Today's patients awaiting consultation.
3. Click **"Consultation Details"** on Eleanor's appointment.
4. Fill out the clinical notes:
   - **Diagnosis**: *"Mild Sinus Bradycardia"*.
   - **Prescription**: *"Aspirin 75mg daily after breakfast"*.
   - **Physician Advice**: *"Regular aerobic walking, follow up in 30 days"*.
5. Click **"Save Clinical Record & Complete Visit"**:
   - The appointment status transitions automatically to `COMPLETED`.
   - The medical record is permanently archived in `patient_records` for Eleanor's medical history.

---

## 5. The Core Technical Masteries (What Sir Will Scrutinize)

---

### 5.1 Duplicate Booking Prevention & Pessimistic Locking

#### The Problem:
If two patients submit booking requests for the same slot at the exact same millisecond:
1. Both read `slot.status == AVAILABLE`.
2. Both proceed to create an appointment.
3. The doctor is double-booked.

#### The MediSlot Three-Tier Solution:
1. **Tier 1 (Transaction Isolation)**:
   The booking method in `AppointmentServiceImpl` is annotated with `@Transactional(isolation = Isolation.REPEATABLE_READ)`.
2. **Tier 2 (Pessimistic Row Lock — `SELECT FOR UPDATE`)**:
   Instead of a normal query, we run:
   ```sql
   SELECT * FROM appointment_slots WHERE slot_id = ? FOR UPDATE;
   ```
   In MySQL InnoDB, this acquires an **exclusive write lock (X-Lock)** on that specific slot row. The second transaction is physically forced to wait until the first transaction commits or rolls back. When Transaction 2 acquires the lock, it sees `status = BOOKED` and is rejected with `SlotUnavailableException`.
3. **Tier 3 (Database Constraint Safety Net)**:
   The `appointment_slots` table has a composite unique key:
   ```sql
   UNIQUE KEY uq_doctor_date_time (doctor_id, slot_date, start_time)
   ```
   Even if an application thread somehow bypassed the lock, MySQL would abort the insert with an `IntegrityConstraintViolationException`.

---

### 5.2 Traditional JDBC vs. Spring JdbcTemplate

Our project implements both data access technologies to contrast them directly:

```
          TRADITIONAL JDBC                                SPRING JDBCTEMPLATE
(TraditionalPatientJdbcRepository.java)                 (JdbcPatientRepository.java)
┌──────────────────────────────────────┐       ┌──────────────────────────────────────┐
│ • Manual Connection from DataSource  │       │ • Managed Connection from HikariCP   │
│ • Manual PreparedStatement creation  │       │ • Template Method Design Pattern     │
│ • Explicit 1-based index binding     │       │ • Automatic varargs parameter binding│
│ • Manual while(rs.next()) loop       │       │ • Functional RowMapper<T> extraction │
│ • Checked SQLException handling      │       │ • Unchecked DataAccessException      │
│ • ~45 lines of code per query        │       │ • ~7 lines of code per query         │
│ • Vulnerable to connection leaks     │       │ • Guaranteed leak-free closure       │
└──────────────────────────────────────┘       └──────────────────────────────────────┘
```

#### Code Comparison Snippet:

**Traditional JDBC (`TraditionalPatientJdbcRepository.java`)**:
```java
// Requires try-with-resources on Connection, PreparedStatement, and ResultSet
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients WHERE patient_id = ?")) {
    ps.setLong(1, patientId); // 1-based index
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            Patient p = new Patient();
            p.setPatientId(rs.getLong("patient_id"));
            p.setFullName(rs.getString("full_name"));
            // ... manually extract 10 more columns
            return Optional.of(p);
        }
    }
} catch (SQLException e) { // Checked exception forced by compiler
    throw new RuntimeException("Database error: " + e.getMessage(), e);
}
```

**Spring JdbcTemplate (`JdbcPatientRepository.java`)**:
```java
// Handled in a single expressive call with a reusable RowMapper
String sql = "SELECT * FROM patients WHERE patient_id = ?";
return jdbcTemplate.queryForObject(sql, patientRowMapper, patientId);
```

---

### 5.3 Plain Old Java Object (POJO) vs. Spring-Managed Bean

| Property | POJO (`AppointmentSummaryPOJO.java`) | Spring Bean (`AppointmentServiceImpl.java`) |
|---|---|---|
| **Instantiation** | Explicitly created by developer using `new` operator | Instantiated by Spring IoC Container via reflection |
| **Lifecycle** | Governed by standard Java Garbage Collector | Managed by Spring `ApplicationContext` (init, `@PostConstruct`, `@PreDestroy`) |
| **Scope** | Transient (lives on heap until dereferenced) | **Singleton** by default (one shared instance across the entire application) |
| **Dependency Injection**| None (dependencies must be passed manually) | Injected automatically using Constructor Injection (`@Autowired`) |
| **AOP & Proxies** | None (methods execute directly on CPU) | Wrapped in **Dynamic CGLIB/JDK Proxies** to provide `@Transactional` interceptors |
| **Annotations** | Zero framework annotations | `@Service`, `@Repository`, `@Transactional`, `@Autowired` |
| **Primary Role** | Holding encapsulated domain data | Executing stateful business logic and database transactions |

---

### 5.4 The 4 Pillars of OOP in MediSlot

#### 1. Encapsulation:
- All domain entity fields are `private`. Access is provided via getters and setters.
- Invariants are verified inside entities:
  - `Patient.getAge()` dynamically computes age using `Period.between(dateOfBirth, LocalDate.now())` rather than storing a stale number.
  - `AppointmentSlot.isAvailable()` and `canBeBooked()` encapsulate slot validity rules.
- Data formatting logic is encapsulated inside `AppointmentSummaryPOJO.getFormattedSummary()`.

#### 2. Inheritance:
- Abstract base class `User.java` provides common identity attributes (`userId`, `username`, `passwordHash`, `email`, `phone`, `role`, `createdAt`).
- Subclasses specialize the base class:
  - `Doctor extends User` (adds `specialization`, `consultationFee`, `departmentId`).
  - `Patient extends User` (adds `patientCode`, `dateOfBirth`, `bloodGroup`, `emergencyContact`).
  - `Receptionist extends User` (adds `deskLocation`, `shiftType`).
  - `Admin extends User` (adds `superAdmin`).
- Constructors use `super(...)` to chain initialization to the base class.

#### 3. Polymorphism:
- **Dynamic (Runtime) Polymorphism / Method Overriding**:
  - `User.java` defines abstract methods: `getDashboardUrl()` and `getRoleDescription()`.
  - At runtime, calling `user.getDashboardUrl()` dynamically routes `Admin` to `/admin/dashboard`, `Doctor` to `/doctor/dashboard`, `Receptionist` to `/receptionist/dashboard`, and `Patient` to `/patient/dashboard`.
  - Interface-driven repositories (`PatientRepository`) allow polymorphic swapping of implementations.
- **Static (Compile-Time) Polymorphism / Method Overloading**:
  - `TraditionalPatientJdbcRepository` implements overloaded query methods:
    - `findById(Long patientId)`
    - `findByPatientCode(String patientCode)`
    - `findByEmailOrPhone(String identifier)`
  - Overloaded entity constructors across domain models.

#### 4. Abstraction:
- High-level controllers interact strictly with Service interfaces (`AppointmentService`, `PatientService`), having zero knowledge of SQL or database drivers.
- Service implementations interact strictly with Repository interfaces (`AppointmentRepository`, `DoctorRepository`), decoupling business rules from persistence mechanisms.

---

## 6. Database Schema & Entity Relationships

MediSlot utilizes 10 normalized tables in MySQL:

```
┌──────────────┐       1:1       ┌──────────────┐
│    users     ├─────────────────┤   patients   │
└──────┬───────┘                 └──────┬───────┘
       │                                │ 1:N
       │ 1:1                            ▼
       │                         ┌──────────────┐
       ├─────────────────────────┤ appointments │
       │                         └──────▲───────┘
       │ 1:1                            │ N:1
       ▼                                │
┌──────────────┐       1:N       ┌──────┴───────────────┐
│   doctors    ├─────────────────┤  appointment_slots   │
└──────┬───────┘                 └──────────────────────┘
       │ 1:N
       ▼
┌──────────────────────┐
│   doctor_schedules   │
└──────────────────────┘
```

1. **`users`**: Base credentials, passwords (BCrypt hashed), and roles (`ADMIN`, `DOCTOR`, `RECEPTIONIST`, `PATIENT`).
2. **`departments`**: Clinical departments (Cardiology, Dermatology, Orthopedics, Pediatrics, Neurology, General Medicine).
3. **`doctors`**: Physician qualifications, experience, fees, linked to `users(user_id)` and `departments(department_id)`.
4. **`patients`**: Demographic records, unique patient code (`PAT-yyyyMM-XXXX`), linked to `users(user_id)`.
5. **`doctor_schedules`**: Recurring weekly shifts (Monday to Sunday, start time, end time, 30-min slot duration).
6. **`appointment_slots`**: Bookable calendar slots with status (`AVAILABLE`, `BOOKED`, `CANCELLED`) and `UNIQUE KEY (doctor_id, slot_date, start_time)`.
7. **`appointments`**: Booked consultation vouchers with reference (`APT-yyyyMMdd-XXXXX`) and status lifecycle (`BOOKED` → `CONFIRMED` → `CHECKED_IN` → `COMPLETED` / `CANCELLED`).
8. **`patient_records`**: Clinical medical records: diagnosis, prescription, doctor advice, created upon visit completion.
9. **`waiting_list`**: Standby patient queue for fully booked doctor shifts.
10. **`audit_logs`**: System audit trail tracking critical operations with timestamps.

---

## 7. Codebase Map: Where Everything Lives

If Sir asks you: *"Where is the code for X?"*, here is your cheat sheet:

```
G:\Krish CU\OOPs project\src\main\java\com\medislot\
├── MediSlotApplication.java             <-- Main Spring Boot entry point
├── config/
│   ├── SecurityConfig.java              <-- Spring Security, BCrypt, role-based URL guards
│   ├── DataInitializer.java            <-- Auto-seeds DB schema and 14 days of slots on boot
│   └── CustomUserDetails.java           <-- Adapter between our User model & Spring Security
├── controller/
│   ├── AdminController.java             <-- Operations dashboard, doctors, patients, demo routes
│   ├── DoctorController.java            <-- Doctor schedule queue, clinical consultation records
│   ├── ReceptionistController.java      <-- Check-ins, walk-in registration, rescheduling
│   ├── PatientController.java           <-- Booking wizard, confirmation slips, profile
│   └── GlobalExceptionHandler.java      <-- Translates custom exceptions to user error alerts
├── model/
│   ├── User.java                        <-- Abstract base class (Polymorphism & Inheritance)
│   ├── Doctor.java, Patient.java        <-- Specialized actor subclasses
│   ├── Appointment.java, Slot.java      <-- Domain entities
│   └── AppointmentSummaryPOJO.java      <-- Plain Old Java Object for viva contrast
├── repository/
│   ├── traditional/
│   │   └── TraditionalPatientJdbcRepository.java <-- Raw Traditional JDBC APIs
│   └── jdbc/
│       ├── JdbcAppointmentRepository.java     <-- Spring JdbcTemplate + RowMappers
│       └── JdbcAppointmentSlotRepository.java <-- SELECT FOR UPDATE pessimistic locking
└── service/
    └── impl/
        └── AppointmentServiceImpl.java  <-- @Transactional booking, rescheduling, slot release
```

---

## 8. Automated Test Suite & Concurrency Verification

All tests compile and pass with **0 failures and 0 errors**:

```powershell
# Run the entire test suite:
mvn test

# Run only the concurrency race-condition test:
mvn test -Dtest=ConcurrencyBookingTest
```

### The Multithreaded Test Explained (`ConcurrencyBookingTest.java`):
1. **Setup**: Creates a single available consultation slot for Dr. Arjun Mehta on a future date.
2. **Execution**: Initializes an `ExecutorService` with 2 threads and synchronizes them using a `CountDownLatch(1)` starting gate.
3. **Fire**: Both threads fire `appointmentService.bookAppointment()` for the exact same slot ID at the exact same millisecond.
4. **Assertions**:
   - `assertEquals(1, successCount.get())`: Exactly **one** transaction succeeds and creates an appointment.
   - `assertEquals(1, failureCount.get())`: The competing thread is safely rejected with `SlotUnavailableException`.
   - `assertEquals(SlotStatus.BOOKED, slot.getStatus())`: Slot remains cleanly booked without data corruption.

---

## 9. The Ultimate Viva Defense Cheat Sheet (25+ Q&As)

### Core Java & OOP Questions

#### Q1: Why did you make `User` an abstract class instead of an interface?
> **Answer**: `User` represents the core identity shared by all actors in our hospital system. It holds mutable **state** (`userId`, `username`, `passwordHash`, `email`, `phone`, `createdAt`) and common implementation logic (getters and setters). Interfaces cannot hold instance state. By using an abstract class, subclasses (`Doctor`, `Patient`, etc.) inherit shared state while being forced to override abstract behavioral methods (`getDashboardUrl()` and `getRoleDescription()`).

#### Q2: Where is Runtime Polymorphism demonstrated in MediSlot?
> **Answer**: In our authentication and routing flow. The base class `User` declares the abstract method `getDashboardUrl()`. Subclasses override it: `Admin` returns `/admin/dashboard`, `Doctor` returns `/doctor/dashboard`, `Receptionist` returns `/receptionist/dashboard`, and `Patient` returns `/patient/dashboard`. When the user logs in, Spring invokes `user.getDashboardUrl()`. At runtime, dynamic method dispatch invokes the subclass-specific implementation without needing `instanceof` checks.

#### Q3: What is the difference between Method Overloading and Method Overriding in your code?
> **Answer**:
> - **Method Overloading** (Compile-time polymorphism) is in `TraditionalPatientJdbcRepository` where `findById(Long)` and `findByPatientCode(String)` have the same name but different parameter types.
> - **Method Overriding** (Runtime polymorphism) is where `Doctor` and `Patient` override `getDashboardUrl()` and `getRoleDescription()` from `User.java`.

#### Q4: Why did you extend `RuntimeException` for your custom exceptions instead of `Exception`?
> **Answer**: `RuntimeException` represents unchecked exceptions. In modern Spring architectures, unchecked exceptions eliminate repetitive `try-catch` boilerplate across service and controller layers. Furthermore, Spring's declarative `@Transactional` rolls back transactions automatically upon encountering unchecked exceptions, whereas checked exceptions require explicit rollback configuration.

---

### Spring Boot & Spring MVC Questions

#### Q5: What is Inversion of Control (IoC) and Dependency Injection (DI)?
> **Answer**: IoC means the control of creating and managing objects is transferred from our application code to the Spring Container (`ApplicationContext`). Dependency Injection is the pattern where Spring provides dependencies to a class (via constructor injection) rather than the class creating them with `new`. For example, `AppointmentServiceImpl` does not instantiate `JdbcAppointmentRepository`; Spring creates both and injects the repository into the service constructor.

#### Q6: Why did you use Constructor Injection instead of Field Injection (`@Autowired` on fields)?
> **Answer**: Constructor injection is the recommended best practice in Spring Boot. It makes dependencies explicit, enforces immutability via `final` fields, prevents `NullPointerException` during testing without needing reflection, and allows mock dependencies to be easily passed during unit tests.

#### Q7: What is the purpose of `@ControllerAdvice` in MediSlot?
> **Answer**: `GlobalExceptionHandler` is annotated with `@ControllerAdvice`. It centralizes exception handling across all MVC controllers. When a custom exception like `SlotUnavailableException` or `DuplicateBookingException` is thrown anywhere in the service layer, the advice intercepts it, converts it into a user-friendly error message, and redirects the user with a flash attribute instead of showing an unhandled error stack trace.

#### Q8: What is the difference between a POJO and a Spring Bean?
> **Answer**: A POJO (`AppointmentSummaryPOJO`) is an unmanaged Java object instantiated via `new`, stored on the JVM heap, and managed by the Garbage Collector. A Spring Bean (`AppointmentServiceImpl`) is managed by Spring's `ApplicationContext`, scoped as a singleton, injected via IoC, and wrapped in dynamic proxies for cross-cutting concerns like `@Transactional`.

---

### Database, JDBC & Concurrency Questions

#### Q9: What is the biggest disadvantage of Traditional JDBC that Spring JdbcTemplate solves?
> **Answer**: Traditional JDBC requires extensive boilerplate code for opening connections, compiling statements, binding positional parameters (`ps.setString(1, ...)`), iterating result cursors (`rs.next()`), and handling checked `SQLException`. More importantly, Traditional JDBC risks **connection leaks** if resources are not closed properly. Spring's `JdbcTemplate` uses the Template Method pattern to guarantee deterministic connection closure, parameter binding, and exception translation.

#### Q10: How does `RowMapper<T>` work in Spring JdbcTemplate?
> **Answer**: `RowMapper<T>` is a functional interface with a single method `mapRow(ResultSet rs, int rowNum)`. It tells `JdbcTemplate` how to map a single row of a database cursor into a Java domain object. Because it is stateless, a single `RowMapper` instance can be reused across all query methods in a repository.

#### Q11: How did you prevent double-booking when two patients request the same doctor slot simultaneously?
> **Answer**: We use pessimistic row-level locking via `SELECT * FROM appointment_slots WHERE slot_id = ? FOR UPDATE` inside a `@Transactional(isolation = Isolation.REPEATABLE_READ)` boundary. In MySQL InnoDB, this acquires an exclusive lock on that slot row. The second transaction is forced to wait until the first commits, at which point it sees the slot is already `BOOKED` and throws `SlotUnavailableException`. As a secondary safety net, the table has a composite `UNIQUE KEY (doctor_id, slot_date, start_time)`.

#### Q12: What happens when an appointment is rescheduled or cancelled?
> **Answer**: In `AppointmentServiceImpl`, cancellations and rescheduling are transaction-safe. When cancelled, the appointment is marked `CANCELLED` and its associated slot status is transitioned back to `AVAILABLE`. When rescheduled, the original slot is released back to `AVAILABLE` while the requested new slot is locked and marked `BOOKED`. If any step fails, the entire transaction rolls back.

#### Q13: What is the difference between Optimistic Locking and Pessimistic Locking?
> **Answer**:
> - **Optimistic Locking** assumes conflicts are rare. It uses a version column (`@Version`) and checks if the version changed during update. If changed, an `OptimisticLockException` is thrown.
> - **Pessimistic Locking** (`SELECT FOR UPDATE`) assumes conflicts will happen. It locks the database row immediately upon read, preventing any other transaction from reading or modifying it until the current transaction commits. We chose pessimistic locking because appointment slots are high-contention resources during peak booking times.

---

### Web, Security & UI Questions

#### Q14: How are passwords secured in MediSlot?
> **Answer**: Passwords are never stored in plain text. We configure Spring Security's `BCryptPasswordEncoder` with a work factor of 10. BCrypt incorporates a random 16-byte salt to prevent rainbow table attacks and uses an adaptive key derivation function that is resistant to brute-force cracking.

#### Q15: How is role-based authorization enforced?
> **Answer**: In `SecurityConfig.java`, we configure URL authorization rules using `.requestMatchers()`:
> - `/admin/**` requires `ROLE_ADMIN`
> - `/doctor/**` requires `ROLE_DOCTOR`
> - `/receptionist/**` requires `ROLE_RECEPTIONIST`
> - `/patient/**` requires `ROLE_PATIENT`
> Unauthorized requests are rejected with HTTP 403 Forbidden.

#### Q16: How is the application made mobile-friendly?
> **Answer**: We implemented a mobile-first responsive design system using CSS media queries (`max-width: 768px`). On mobile devices, the fixed sidebar smoothly collapses into an offcanvas drawer that slides out when tapping the topbar hamburger menu (`☰`), dashboard metric grids stack into a single column, form grids stack vertically, and tables support horizontal touch-scrolling without breaking layouts.

---

*This master document is maintained in the project root for faculty review and team defense preparation.*
