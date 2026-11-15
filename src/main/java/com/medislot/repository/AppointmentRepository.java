package com.medislot.repository;

import com.medislot.model.Appointment;
import com.medislot.model.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AppointmentRepository {
    Optional<Appointment> findById(Long appointmentId);
    Optional<Appointment> findByIdForUpdate(Long appointmentId);
    Optional<Appointment> findByReference(String reference);
    Optional<Appointment> findBySlotId(Long slotId);

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date);
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findUpcomingAppointments(int limit);
    List<Appointment> findAll();

    Long save(Appointment appointment);
    int updateStatus(Long appointmentId, AppointmentStatus status);
    int updateSlotAndDate(Long appointmentId, Long newSlotId, LocalDate newDate, java.time.LocalTime startTime, java.time.LocalTime endTime);

    // Reporting and Dashboard Metrics
    long countAppointmentsByDate(LocalDate date);
    long countUpcomingAppointments();
    long countCompletedAppointments();
    long countCancelledAppointments();
    Map<String, Long> countAppointmentsByStatus();
    List<Map<String, Object>> getDepartmentAppointmentDistribution();
    List<Map<String, Object>> getDoctorAppointmentSummary();
}
