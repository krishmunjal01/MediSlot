# MediSlot — Relational Database Architecture & SQL Guide

This document details the normalized relational database schema designed for **MediSlot**, highlighting primary keys, foreign key constraints, indexes, unique keys, and transactional locking mechanisms.

---

## 1. Database Overview & Entity-Relationship Architecture

Database Name: `medislot_db`  
Storage Engine: **InnoDB** (Mandatory for ACID transactions, row-level locking, and foreign keys)  
Character Set: `utf8mb4` with collation `utf8mb4_unicode_ci`

```
┌─────────────┐       1:N       ┌─────────────────────┐
│ departments │────────────────▶│       doctors       │
└─────────────┘                 └──────────┬──────────┘
                                           │
         ┌─────────────────────────────────┼──────────────────────────────┐
         │ 1:N                             │ 1:N                          │ 1:N
         ▼                                 ▼                              ▼
┌──────────────────┐            ┌─────────────────────┐        ┌──────────────────┐
│ doctor_schedules │            │  appointment_slots  │        │   waiting_list   │
└──────────────────┘            └──────────┬──────────┘        └──────────────────┘
                                           │ 1:1
                                           ▼
┌─────────────┐       1:N       ┌─────────────────────┐       1:1      ┌─────────────────┐
│  patients   │────────────────▶│     appointments    │───────────────▶│ patient_records │
└──────┬──────┘                 └─────────────────────┘                └─────────────────┘
       │ 1:1
       ▼
┌─────────────┐
│    users    │ (Base authentication identity for Admin, Doctor, Receptionist, Patient)
└──────┬──────┘
       │ 1:N
       ▼
┌─────────────┐
│ audit_logs  │
└─────────────┘
```

---

## 2. Table Specifications

### 1. `users`
Base identity table storing authentication credentials, hashed passwords, contact details, and role assignments.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Internal user surrogate key |
| `username` | `VARCHAR(50)` | `NOT NULL UNIQUE` | Unique login handle |
| `email` | `VARCHAR(100)` | `NOT NULL UNIQUE` | Verified email address |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt password hash |
| `role` | `VARCHAR(20)` | `NOT NULL` | Role enum: `ADMIN`, `DOCTOR`, `RECEPTIONIST`, `PATIENT` |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Official legal name |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Primary telephone number |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Soft-disable flag |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp |

---

### 2. `departments`
Hospital clinical divisions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `department_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Department key |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | e.g. Cardiology, Dermatology |
| `code` | `VARCHAR(10)` | `NOT NULL UNIQUE` | e.g. CARD, DERM |
| `description` | `TEXT` | `NULL` | Department scope |
| `head_doctor_name` | `VARCHAR(100)` | `NULL` | Department head |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp |

---

### 3. `doctors`
Medical physician profile.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `doctor_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Physician key |
| `user_id` | `BIGINT` | `NOT NULL UNIQUE FK -> users(user_id)` | Link to user login credentials |
| `department_id` | `BIGINT` | `NOT NULL FK -> departments(department_id)` | Clinical department |
| `specialization` | `VARCHAR(100)` | `NOT NULL` | e.g. Interventional Cardiologist |
| `qualification` | `VARCHAR(100)` | `NOT NULL` | Medical degrees (MBBS, MD, FRCS) |
| `experience_years` | `INT` | `NOT NULL DEFAULT 0` | Clinical practice experience |
| `consultation_fee` | `DECIMAL(10,2)` | `NOT NULL DEFAULT 500.00` | Consultation room fee |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ACTIVE'` | `ACTIVE`, `INACTIVE`, `ON_LEAVE` |

---

### 4. `patients`
Patient medical demographics.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Patient surrogate key |
| `user_id` | `BIGINT` | `UNIQUE FK -> users(user_id)` | Optional link to user login |
| `patient_code` | `VARCHAR(30)` | `NOT NULL UNIQUE` | Human-readable code (`PAT-202611-1001`) |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Patient name |
| `email` | `VARCHAR(100)` | `NOT NULL` | Contact email |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Mobile phone |
| `date_of_birth` | `DATE` | `NOT NULL` | DOB used for dynamic age calculation |
| `gender` | `VARCHAR(10)` | `NOT NULL` | Gender |
| `blood_group` | `VARCHAR(5)` | `NULL` | Blood group (A+, O-, etc.) |
| `address` | `TEXT` | `NOT NULL` | Residential address |
| `emergency_contact`| `VARCHAR(20)` | `NULL` | Emergency contact phone |
| `registration_date`| `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Registration date |

---

### 5. `doctor_schedules`
Doctor recurring weekly shifts.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `schedule_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Schedule key |
| `doctor_id` | `BIGINT` | `NOT NULL FK -> doctors(doctor_id)` | Doctor assigned |
| `day_of_week` | `VARCHAR(15)` | `NOT NULL` | `MONDAY`, `TUESDAY`, etc. |
| `start_time` | `TIME` | `NOT NULL` | Shift start time |
| `end_time` | `TIME` | `NOT NULL` | Shift end time |
| `slot_duration_minutes` | `INT` | `NOT NULL DEFAULT 30` | Duration per appointment |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active shift flag |

---

### 6. `appointment_slots` (CRITICAL CONCURRENCY TABLE)
Stores individual bookable time slots generated from doctor schedules.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `slot_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Slot surrogate key |
| `doctor_id` | `BIGINT` | `NOT NULL FK -> doctors(doctor_id)` | Attending physician |
| `slot_date` | `DATE` | `NOT NULL` | Calendar date of consultation |
| `start_time` | `TIME` | `NOT NULL` | Slot beginning |
| `end_time` | `TIME` | `NOT NULL` | Slot completion |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'AVAILABLE'` | `AVAILABLE`, `LOCKED`, `BOOKED`, `CANCELLED` |

**Constraints & Indexes**:
- `CONSTRAINT uq_doctor_date_time UNIQUE (doctor_id, slot_date, start_time)`  
  *Prevents duplicate slots at the database storage engine level, making race conditions physically impossible.*
- `INDEX idx_slot_lookup (doctor_id, slot_date, status)`  
  *Accelerates real-time availability lookups by the frontend booking interface.*

---

### 7. `appointments`
Confirmed bookings.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `appointment_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Appointment key |
| `appointment_reference` | `VARCHAR(30)` | `NOT NULL UNIQUE` | Official receipt code (`APT-20261115-XXXX`) |
| `patient_id` | `BIGINT` | `NOT NULL FK -> patients(patient_id)` | Patient |
| `doctor_id` | `BIGINT` | `NOT NULL FK -> doctors(doctor_id)` | Attending doctor |
| `slot_id` | `BIGINT` | `NOT NULL UNIQUE FK -> appointment_slots(slot_id)` | Associated slot |
| `appointment_date` | `DATE` | `NOT NULL` | Date |
| `start_time` | `TIME` | `NOT NULL` | Start time |
| `end_time` | `TIME` | `NOT NULL` | End time |
| `reason_for_visit` | `TEXT` | `NULL` | Symptoms / notes |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'BOOKED'` | `BOOKED`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`, `NO_SHOW` |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Booking timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last updated |

---

### 8. `waiting_list`
Standby queue for fully booked doctor dates.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `waiting_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Queue key |
| `doctor_id` | `BIGINT` | `NOT NULL FK -> doctors(doctor_id)` | Requested doctor |
| `patient_id` | `BIGINT` | `NOT NULL FK -> patients(patient_id)` | Patient waiting |
| `requested_date` | `DATE` | `NOT NULL` | Target date |
| `queue_position` | `INT` | `NOT NULL DEFAULT 1` | Position in line |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'WAITING'` | `WAITING`, `NOTIFIED`, `CONVERTED`, `CANCELLED` |
| `notes` | `TEXT` | `NULL` | Receptionist notes |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Enqueue timestamp |

---

### 9. `patient_records`
Physician medical notes and prescriptions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `record_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Medical record key |
| `appointment_id` | `BIGINT` | `NOT NULL UNIQUE FK -> appointments(appointment_id)` | Consultation visit |
| `patient_id` | `BIGINT` | `NOT NULL FK -> patients(patient_id)` | Patient |
| `doctor_id` | `BIGINT` | `NOT NULL FK -> doctors(doctor_id)` | Authoring physician |
| `symptoms` | `TEXT` | `NULL` | Observed symptoms |
| `diagnosis` | `TEXT` | `NOT NULL` | Official diagnosis |
| `prescription` | `TEXT` | `NOT NULL` | Prescribed medication & dosage |
| `clinical_notes` | `TEXT` | `NULL` | Lifestyle & dietary advice |
| `follow_up_date` | `DATE` | `NULL` | Recommended next visit |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Record timestamp |

---

### 10. `audit_logs`
System operational audit trail.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `log_id` | `BIGINT` | `AUTO_INCREMENT PRIMARY KEY` | Log key |
| `user_id` | `BIGINT` | `NULL FK -> users(user_id)` | User who executed the action |
| `action` | `VARCHAR(50)` | `NOT NULL` | Action code (e.g. `BOOK_APPOINTMENT`) |
| `entity_type` | `VARCHAR(50)` | `NOT NULL` | Entity affected (e.g. `APPOINTMENT`) |
| `entity_id` | `VARCHAR(50)` | `NULL` | ID of affected entity |
| `details` | `TEXT` | `NULL` | Descriptive explanation |
| `ip_address` | `VARCHAR(50)` | `NULL` | Client IP address |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Event timestamp |

---

## 3. Concurrency Protection & Transaction Safety

To satisfy non-negotiable Requirement 6, booking transactions follow this exact sequence:

```sql
-- Step 1: Start Transaction with Repeatable Read Isolation
START TRANSACTION;

-- Step 2: Acquire pessimistic exclusive lock on the requested slot
SELECT * FROM appointment_slots 
WHERE slot_id = 385 
FOR UPDATE;

-- Step 3: Verify slot is AVAILABLE in application code.
-- If another transaction got the lock first and changed status to BOOKED,
-- the current transaction throws SlotUnavailableException.

-- Step 4: Insert the appointment
INSERT INTO appointments (appointment_reference, patient_id, doctor_id, slot_id, appointment_date, start_time, end_time, reason_for_visit, status)
VALUES ('APT-20261115-A1B2C', 1, 1, 385, '2026-11-20', '10:00:00', '10:30:00', 'Checkup', 'BOOKED');

-- Step 5: Transition the slot status to BOOKED
UPDATE appointment_slots 
SET status = 'BOOKED' 
WHERE slot_id = 385;

-- Step 6: Commit transaction, releasing the row lock
COMMIT;
```

Even if two simultaneous HTTP requests execute this code at the exact same millisecond:
1. Thread 1 acquires the row lock on slot 385.
2. Thread 2 is forced to wait for Thread 1's transaction to commit.
3. Thread 1 updates slot 385 to `BOOKED` and commits.
4. Thread 2 unblocks, reads the updated status `BOOKED`, and throws `SlotUnavailableException`.
5. If any bug or raw SQL bypass occurred, the MySQL UNIQUE constraint on `(doctor_id, slot_date, start_time)` rejects the second insert with a duplicate key error.
