# MediSlot — Hospital Appointment & Patient Care Coordination System

> **Object-Oriented Programming Using Java | University Academic Case Study & Capstone Project**  
> Built with **Java 17**, **Spring Boot 3.2**, **Spring MVC**, **Spring JdbcTemplate**, **Traditional JDBC**, **MySQL 8.0**, and **Thymeleaf**.

---

## 1. Executive Summary & Product Direction
**MediSlot** is a hospital operations and patient care coordination platform connecting hospital administrators, attending physicians, front-desk receptionists, and patients through a unified web application.

Unlike generic student CRUD dashboards or bloated hospital ERPs with dozens of dummy buttons, MediSlot delivers four production-grade workflows wrapped in an editorial visual identity inspired by modern European clinical software:
- **Warm Ivory Palette** (`#F8F7F3`), crisp surfaces (`#FFFFFF`), charcoal text (`#292D29`), and subtle forest green accents (`#476B56`).
- **No electric gradients**, no purple/blue glassmorphism, no fake metrics, and no dead buttons.
- **Race-Condition-Proof Concurrency Control**: Uses pessimistic database locks (`SELECT FOR UPDATE`) and MySQL unique constraints to prevent duplicate bookings during simultaneous requests.
- **Direct Side-by-Side Academic Viva Demonstrations**:
  - `/admin/technical-demonstration`: Traditional JDBC vs. Spring JdbcTemplate live execution.
  - `/admin/oop-demonstration`: POJO vs. Spring-managed Bean comparison.

---

## 2. Core Workflows by Role

### 🏥 Administrator (`/admin/*`)
- **Executive Operations Dashboard**: Real-time metrics aggregated via SQL (`COUNT`, `GROUP BY`, table joins).
- **Medical Staff Management**: Register new attending physicians, assign departments, configure experience and fees, and toggle active/inactive status.
- **Patient Demographic Directory**: View full patient records, demographic history, and past clinical consultations.
- **Doctor Shift Scheduling & Slot Generator**: Configure recurring weekly clinic hours and batch-generate forward consultation slots.
- **Department Management**: Add and configure hospital departments (Cardiology, Dermatology, Orthopedics, Pediatrics, Neurology, General Medicine).
- **Appointment Oversight & Reports**: Hospital-wide consultation logs with filterable status indicators and department volume charts.
- **Standby Waiting List Oversight**: Monitor patients waiting for fully booked doctor shifts.
- **Academic Demonstration Pages**: Interactive live comparisons for viva defense.

### 🩺 Doctor (`/doctor/*`)
- **Doctor Daily Dashboard**: Today's scheduled patients, pending arrivals, and completed consultations.
- **Assigned Consultations**: Filterable daily and weekly patient schedule.
- **Clinical Consultation Record**: Document presenting symptoms, formal clinical diagnosis, prescription with dosage instructions, lifestyle notes, and follow-up dates. Saving this marks the appointment `COMPLETED`.
- **Personal Availability Management**: View recurring weekly hours and active slots.

### 📋 Front Desk Receptionist (`/receptionist/*`)
- **Reception Desk Dashboard**: Real-time patient arrival queue and instant check-in button.
- **Patient Registration & Lookup**: Search patients by phone, patient code, email, or name; register walk-in patients.
- **Appointment Booking on Behalf of Patients**: Direct slot booking wizard.
- **Appointment Rescheduling & Cancellation**: Reschedule visits or cancel appointments (automatically releases the slot back to open availability).
- **Waiting List Queue**: Enqueue standby patients and notify them when slots are freed.

### 🧑‍⚕️ Patient (`/patient/*`)
- **Health Home Dashboard**: Upcoming confirmed visits, previous consultation history, and profile summary.
- **Consultation Booking Wizard**: Choose department -> Select doctor -> Pick consultation date -> Interactive time slot buttons -> Reason -> Instant confirmation.
- **Printable Confirmation Slip**: Official appointment voucher with appointment reference, date, time, doctor, room fee, and completed clinical prescription.
- **Self-Service Modification**: Reschedule or cancel eligible appointments.
- **Demographic Profile**: Manage contact details and emergency contact phone.

---

## 3. Technology Stack

| Layer | Technology | Role in Architecture |
|---|---|---|
| **Language** | Java 17 (OpenJDK 17 LTS) | Core Object-Oriented implementation |
| **Framework** | Spring Boot 3.2.5 | Application framework, IoC container, Dependency Injection |
| **Database** | MySQL 8.0 | Relational storage, ACID transaction handling, pessimistic row locking |
| **Persistence** | Spring JdbcTemplate + Traditional JDBC | Direct SQL mapping with `RowMapper` and raw `PreparedStatement` |
| **Security** | Spring Security 6.2 | BCrypt password hashing, session management, role-based URL guards |
| **Frontend** | Thymeleaf 3 + Semantic HTML5 + Custom CSS | Server-side template rendering with editorial healthcare design |
| **Build Tool** | Apache Maven 3.9 | Dependency resolution and lifecycle management |
| **Testing** | JUnit 5 Jupiter + Spring Boot Test | Automated unit, integration, and multithreaded concurrency tests |

---

## 4. Academic Case Study Requirements Fulfilled

1. **Patient Registration & Medical Records**: Unique patient codes (`PAT-yyyyMM-XXXX`), full demographic profile, visit history, and physician clinical notes.
2. **Doctor Management**: Fictional doctors across 6 departments with specialization, qualifications, experience, and fee schedules.
3. **Doctor Schedules & Slots**: Weekly recurring shift definitions, non-overlapping validation, and slot generator.
4. **Appointment Booking & Safe Transitions**: Status lifecycle (`BOOKED` → `CONFIRMED` → `CHECKED_IN` → `COMPLETED` / `CANCELLED` / `NO_SHOW`).
5. **Modify & Cancel**: Transactional slot release back to `AVAILABLE` upon cancellation or rescheduling.
6. **Duplicate Booking Prevention**: Database-level `UNIQUE KEY (doctor_id, slot_date, start_time)` combined with pessimistic `SELECT FOR UPDATE` locking inside a `@Transactional(isolation = Isolation.REPEATABLE_READ)` boundary.
7. **Traditional JDBC Implementation**: Complete raw JDBC repository (`TraditionalPatientJdbcRepository.java`) demonstrating `Connection`, `PreparedStatement`, `ResultSet`, parameter binding, and `try-with-resources`.
8. **Spring JdbcTemplate Implementation**: Dedicated repositories utilizing `JdbcTemplate.query()`, `queryForObject()`, and `RowMapper<T>`.
9. **POJO vs. Spring Bean Viva Comparison**: Contrast between unmanaged `AppointmentSummaryPOJO` (JVM heap `new`) and Spring `@Service` container-managed singleton (`AppointmentServiceImpl`).
10. **Role-Based Workflows**: Independent, secured dashboards for Administrator, Doctor, Receptionist, and Patient.

---

## 5. Demo Accounts for University Viva Evaluation

All demo accounts are pre-seeded automatically by `DataInitializer.java` upon initial boot:

| Role | Username | Password | Purpose & Sample Name |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full hospital oversight (Marcus Aurelius) |
| **Doctor** | `dr.arjun` | `doctor123` | Attending Cardiologist (Dr. Arjun Mehta) |
| **Doctor** | `dr.priya` | `doctor123` | Attending Dermatologist (Dr. Priya Sharma) |
| **Receptionist** | `receptionist` | `recep123` | Front desk coordinator (Clara Oswald) |
| **Patient** | `eleanor` | `patient123` | Registered Patient (Eleanor Vance) |
| **Patient** | `marcus` | `patient123` | Registered Patient (Marcus Brody) |

*Note: The login page at `http://localhost:8080/login` includes one-click quick-fill buttons for each demo role.*

---

## 6. Project Directory Hierarchy
```
g:\Krish CU\OOPs project\
├── pom.xml
├── README.md
├── SETUP.md
├── DATABASE_SETUP.md
├── OOP_CONCEPTS.md
├── JDBC_COMPARISON.md
├── TESTING.md
├── src/
│   ├── main/
│   │   ├── java/com/medislot/
│   │   │   ├── MediSlotApplication.java
│   │   │   ├── config/
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AppointmentApiController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── PatientController.java
│   │   │   │   └── ReceptionistController.java
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   │   ├── User.java (Abstract Base)
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Receptionist.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── AppointmentSlot.java
│   │   │   │   ├── AppointmentSummaryPOJO.java
│   │   │   │   └── enums/
│   │   │   ├── repository/
│   │   │   │   ├── interfaces/
│   │   │   │   ├── jdbc/ (Spring JdbcTemplate)
│   │   │   │   └── traditional/ (Raw Traditional JDBC)
│   │   │   └── service/
│   │   │       ├── interfaces/
│   │   │       └── impl/ (Transactional Business Logic)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application.properties.example
│   │       ├── schema.sql
│   │       ├── static/ (css/medislot.css, js/medislot.js)
│   │       └── templates/ (auth/, admin/, doctor/, receptionist/, patient/, fragments/, error/)
│   └── test/
│       └── java/com/medislot/
│           ├── AppointmentServiceTest.java
│           ├── ConcurrencyBookingTest.java
│           └── TraditionalJdbcTest.java
```

---

## 7. Recommended Demonstration Sequence for Evaluation

1. **Sign In as Admin (`admin` / `admin123`)**:
   - Inspect the operations dashboard metrics (derived from live MySQL queries).
   - Navigate to `/admin/technical-demonstration` to show the side-by-side execution of Traditional JDBC vs. Spring JdbcTemplate.
   - Navigate to `/admin/oop-demonstration` to show the POJO vs. Spring Bean comparison.
2. **Sign In as Patient (`eleanor` / `patient123`)**:
   - Go to "Book Consultation" (`/patient/book`).
   - Select a doctor and date; notice the available slot buttons loaded via live schedule.
   - Confirm booking; view the official appointment voucher with reference code.
3. **Demonstrate Duplicate Booking Prevention (Viva Test Case)**:
   - Try reserving the exact same slot in another tab or test runner. Notice that the system rejects the second request and informs the user that the slot is already taken.
   - View `ConcurrencyBookingTest.java` to demonstrate multithreaded test verification.
4. **Sign In as Doctor (`dr.arjun` / `doctor123`)**:
   - View assigned patients in the consultation queue.
   - Open Eleanor's appointment, record diagnosis, prescription, and notes.
   - Submit consultation record; observe appointment automatically marked `COMPLETED`.
5. **Sign In as Receptionist (`receptionist` / `recep123`)**:
   - Perform one-click patient check-in.
   - Demonstrate rescheduling an appointment, observing the original slot returning to `AVAILABLE`.
