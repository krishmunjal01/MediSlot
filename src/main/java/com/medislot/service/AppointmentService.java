package com.medislot.service;

import com.medislot.dto.AppointmentBookingRequest;
import com.medislot.dto.AppointmentRescheduleRequest;
import com.medislot.model.Appointment;
import com.medislot.model.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /**
     * Books an appointment with strict transactional isolation and pessimistic locking.
     * Prevents race conditions and duplicate slot reservations.
     */
    Appointment bookAppointment(AppointmentBookingRequest request, Long currentUserId);

    /**
     * Reschedules an appointment to a new slot with transactional integrity.
     * Releases old slot back to AVAILABLE and locks new slot to BOOKED.
     */
    Appointment rescheduleAppointment(AppointmentRescheduleRequest request, Long currentUserId);

    /**
     * Cancels an appointment, transitions status to CANCELLED, and releases the slot to AVAILABLE.
     */
    Appointment cancelAppointment(Long appointmentId, String reason, Long currentUserId);

    /**
     * Updates an appointment's operational status (CONFIRMED, CHECKED_IN, COMPLETED, NO_SHOW).
     */
    Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus, Long currentUserId);

    Appointment getAppointmentById(Long appointmentId);
    Appointment getAppointmentByReference(String reference);

    List<Appointment> getAppointmentsForPatient(Long patientId);
    List<Appointment> getAppointmentsForDoctor(Long doctorId);
    List<Appointment> getAppointmentsForDoctorAndDate(Long doctorId, LocalDate date);
    List<Appointment> getAppointmentsForToday();
    List<Appointment> getUpcomingAppointments(int limit);
    List<Appointment> getAllAppointments();
}
