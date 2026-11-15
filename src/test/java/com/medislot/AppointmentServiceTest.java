package com.medislot;

import com.medislot.dto.AppointmentBookingRequest;
import com.medislot.dto.AppointmentRescheduleRequest;
import com.medislot.exception.InvalidAppointmentStateException;
import com.medislot.exception.SlotUnavailableException;
import com.medislot.model.Appointment;
import com.medislot.model.AppointmentSlot;
import com.medislot.model.Doctor;
import com.medislot.model.Patient;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.model.enums.SlotStatus;
import com.medislot.repository.AppointmentSlotRepository;
import com.medislot.repository.DoctorRepository;
import com.medislot.repository.PatientRepository;
import com.medislot.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentSlotRepository slotRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        LocalDate date1 = LocalDate.now().plusDays(20);
        LocalDate date2 = LocalDate.now().plusDays(21);
        jdbcTemplate.update("DELETE FROM appointments WHERE appointment_date IN (?, ?)", java.sql.Date.valueOf(date1), java.sql.Date.valueOf(date2));
        jdbcTemplate.update("DELETE FROM appointment_slots WHERE slot_date IN (?, ?)", java.sql.Date.valueOf(date1), java.sql.Date.valueOf(date2));
    }

    @Test
    @DisplayName("Lifecycle Test: Book -> Reschedule -> Cancel -> Slot Released")
    void testAppointmentLifecycleAndSlotRelease() {
        Doctor doctor = doctorRepository.findAll().get(0);
        Patient patient = patientRepository.findAll().get(0);

        LocalDate date1 = LocalDate.now().plusDays(20);
        LocalDate date2 = LocalDate.now().plusDays(21);

        // 1. Create two test slots
        AppointmentSlot slot1 = new AppointmentSlot(null, doctor.getDoctorId(), date1,
                LocalTime.of(11, 0), LocalTime.of(11, 30), SlotStatus.AVAILABLE);
        Long slot1Id = slotRepository.save(slot1);

        AppointmentSlot slot2 = new AppointmentSlot(null, doctor.getDoctorId(), date2,
                LocalTime.of(14, 0), LocalTime.of(14, 30), SlotStatus.AVAILABLE);
        Long slot2Id = slotRepository.save(slot2);

        // 2. Book Slot 1
        AppointmentBookingRequest bookReq = new AppointmentBookingRequest(
                patient.getPatientId(), doctor.getDoctorId(), slot1Id, date1, "Initial Consultation"
        );
        Appointment booked = appointmentService.bookAppointment(bookReq, patient.getUserId());
        assertNotNull(booked.getAppointmentId());
        assertEquals(AppointmentStatus.BOOKED, booked.getStatus());

        // Verify slot 1 is BOOKED
        assertEquals(SlotStatus.BOOKED, slotRepository.findById(slot1Id).orElseThrow().getStatus());

        // 3. Attempting to book Slot 1 again should fail with SlotUnavailableException
        assertThrows(SlotUnavailableException.class, () -> {
            appointmentService.bookAppointment(bookReq, patient.getUserId());
        });

        // 4. Reschedule to Slot 2
        AppointmentRescheduleRequest reschedReq = new AppointmentRescheduleRequest(
                booked.getAppointmentId(), slot2Id, date2, "Preferred afternoon slot"
        );
        Appointment rescheduled = appointmentService.rescheduleAppointment(reschedReq, patient.getUserId());
        assertEquals(slot2Id, rescheduled.getSlotId());

        // Verify slot 1 was RELEASED back to AVAILABLE
        assertEquals(SlotStatus.AVAILABLE, slotRepository.findById(slot1Id).orElseThrow().getStatus());
        // Verify slot 2 is now BOOKED
        assertEquals(SlotStatus.BOOKED, slotRepository.findById(slot2Id).orElseThrow().getStatus());

        // 5. Cancel Appointment
        Appointment cancelled = appointmentService.cancelAppointment(rescheduled.getAppointmentId(), "Patient recovered", patient.getUserId());
        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());

        // Verify slot 2 was RELEASED back to AVAILABLE
        assertEquals(SlotStatus.AVAILABLE, slotRepository.findById(slot2Id).orElseThrow().getStatus());

        // 6. Attempting to cancel already cancelled appointment throws InvalidAppointmentStateException
        assertThrows(InvalidAppointmentStateException.class, () -> {
            appointmentService.cancelAppointment(cancelled.getAppointmentId(), "Again", patient.getUserId());
        });
    }
}