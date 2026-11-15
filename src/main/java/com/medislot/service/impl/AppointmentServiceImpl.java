package com.medislot.service.impl;

import com.medislot.dto.AppointmentBookingRequest;
import com.medislot.dto.AppointmentRescheduleRequest;
import com.medislot.exception.AppointmentNotFoundException;
import com.medislot.exception.DuplicateBookingException;
import com.medislot.exception.InvalidAppointmentStateException;
import com.medislot.exception.PatientNotFoundException;
import com.medislot.exception.SlotUnavailableException;
import com.medislot.model.Appointment;
import com.medislot.model.AppointmentSlot;
import com.medislot.model.Patient;
import com.medislot.model.WaitingListEntry;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.model.enums.SlotStatus;
import com.medislot.model.enums.WaitingStatus;
import com.medislot.repository.AppointmentRepository;
import com.medislot.repository.AppointmentSlotRepository;
import com.medislot.repository.PatientRepository;
import com.medislot.repository.WaitingListRepository;
import com.medislot.service.AppointmentService;
import com.medislot.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * CRITICAL TECHNICAL CORE: APPOINTMENT SERVICE WITH CONCURRENCY PROTECTION
 * ============================================================================
 * Implements non-negotiable faculty requirements:
 * 1. Pessimistic Row-Level Locking (SELECT ... FOR UPDATE) inside transaction.
 * 2. Database-level UNIQUE constraint on (doctor_id, slot_date, start_time).
 * 3. Atomic transition of slot from AVAILABLE -> BOOKED.
 * 4. Slot release on cancellation/rescheduling back to AVAILABLE.
 * 5. Automatic notification check for waiting-list patients on slot release.
 * ============================================================================
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final WaitingListRepository waitingListRepository;
    private final AuditService auditService;

    // Constructor-based Dependency Injection (Spring Best Practice)
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  AppointmentSlotRepository slotRepository,
                                  PatientRepository patientRepository,
                                  WaitingListRepository waitingListRepository,
                                  AuditService auditService) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.patientRepository = patientRepository;
        this.waitingListRepository = waitingListRepository;
        this.auditService = auditService;
    }

    /**
     * TRANSACTIONAL APPOINTMENT BOOKING:
     * - Isolation Level: REPEATABLE_READ ensures row locks remain held until commit.
     * - Row Lock: slotRepository.findByIdForUpdate(slotId) locks the slot record in MySQL.
     * - Rollback: Any exception triggers an automatic rollback of the slot status and booking.
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Appointment bookAppointment(AppointmentBookingRequest request, Long currentUserId) {
        log.info("Initiating booking transaction for patientId={}, doctorId={}, slotId={}",
                request.getPatientId(), request.getDoctorId(), request.getSlotId());

        // 1. Verify Patient Existence
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        // 2. Lock and fetch requested slot using SELECT FOR UPDATE
        AppointmentSlot slot = slotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() -> new SlotUnavailableException("The requested appointment slot does not exist."));

        // 3. Concurrency check: Ensure slot is AVAILABLE
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            log.warn("Slot id={} is not available (Current status: {})", slot.getSlotId(), slot.getStatus());
            throw new SlotUnavailableException("The selected time slot is already reserved. Please choose another slot.");
        }

        // 4. Validate doctor consistency
        if (!slot.getDoctorId().equals(request.getDoctorId())) {
            throw new IllegalArgumentException("Slot doctor ID does not match request doctor ID.");
        }

        // 5. Validate date is not in the past
        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book appointments for past dates.");
        }

        // 6. Generate Unique Appointment Reference
        String refDate = slot.getSlotDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortCode = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        String reference = "APT-" + refDate + "-" + shortCode;

        // 7. Create Appointment Record
        Appointment appointment = new Appointment();
        appointment.setAppointmentReference(reference);
        appointment.setPatientId(patient.getPatientId());
        appointment.setDoctorId(slot.getDoctorId());
        appointment.setSlotId(slot.getSlotId());
        appointment.setAppointmentDate(slot.getSlotDate());
        appointment.setStartTime(slot.getStartTime());
        appointment.setEndTime(slot.getEndTime());
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        try {
            Long appointmentId = appointmentRepository.save(appointment);
            appointment.setAppointmentId(appointmentId);

            // 8. Update Slot Status to BOOKED
            slotRepository.updateStatus(slot.getSlotId(), SlotStatus.BOOKED);

            auditService.log(currentUserId, "BOOK_APPOINTMENT", "APPOINTMENT",
                    String.valueOf(appointmentId), "Booked appointment " + reference + " for patient " + patient.getFullName());

            log.info("Appointment successfully booked with reference: {}", reference);
            return appointment;

        } catch (DuplicateKeyException e) {
            log.error("Duplicate key violation during slot booking for slotId={}: {}", slot.getSlotId(), e.getMessage());
            throw new DuplicateBookingException("This slot was reserved by another user concurrently. Please select an alternate slot.", e);
        }
    }

    /**
     * TRANSACTIONAL RESCHEDULING:
     * Releases original slot and claims new slot atomically.
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Appointment rescheduleAppointment(AppointmentRescheduleRequest request, Long currentUserId) {
        log.info("Initiating reschedule transaction for appointmentId={}, newSlotId={}",
                request.getAppointmentId(), request.getNewSlotId());

        Appointment appointment = appointmentRepository.findByIdForUpdate(request.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(request.getAppointmentId()));

        if (!appointment.getStatus().canBeRescheduled()) {
            throw new InvalidAppointmentStateException("Appointment cannot be rescheduled in its current status: " + appointment.getStatus());
        }

        Long oldSlotId = appointment.getSlotId();

        // Lock and verify new slot
        AppointmentSlot newSlot = slotRepository.findByIdForUpdate(request.getNewSlotId())
                .orElseThrow(() -> new SlotUnavailableException("The selected new slot does not exist."));

        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotUnavailableException("The new time slot is no longer available. Please select another slot.");
        }

        if (newSlot.getSlotDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot reschedule to a past date.");
        }

        // Release old slot back to AVAILABLE
        slotRepository.updateStatus(oldSlotId, SlotStatus.AVAILABLE);

        // Lock new slot to BOOKED
        slotRepository.updateStatus(newSlot.getSlotId(), SlotStatus.BOOKED);

        // Update appointment details
        appointmentRepository.updateSlotAndDate(appointment.getAppointmentId(), newSlot.getSlotId(),
                newSlot.getSlotDate(), newSlot.getStartTime(), newSlot.getEndTime());

        auditService.log(currentUserId, "RESCHEDULE_APPOINTMENT", "APPOINTMENT",
                String.valueOf(appointment.getAppointmentId()),
                "Rescheduled appointment " + appointment.getAppointmentReference() + " to " + newSlot.getSlotDate() + " at " + newSlot.getStartTime());

        return getAppointmentById(appointment.getAppointmentId());
    }

    /**
     * TRANSACTIONAL CANCELLATION & SLOT RELEASE:
     * Transitions appointment to CANCELLED and marks slot AVAILABLE.
     */
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Appointment cancelAppointment(Long appointmentId, String reason, Long currentUserId) {
        log.info("Initiating cancellation transaction for appointmentId={}", appointmentId);

        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.getStatus().canBeCancelled()) {
            throw new InvalidAppointmentStateException("Appointment cannot be cancelled in status: " + appointment.getStatus());
        }

        // Update appointment status to CANCELLED
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CANCELLED);

        // Release the slot back to AVAILABLE
        slotRepository.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE);

        // Check if there are patients in the waiting list for this doctor & date
        List<WaitingListEntry> waitingEntries = waitingListRepository.findByDoctorAndDate(
                appointment.getDoctorId(), appointment.getAppointmentDate());
        if (!waitingEntries.isEmpty()) {
            WaitingListEntry next = waitingEntries.get(0);
            waitingListRepository.updateStatus(next.getWaitingId(), WaitingStatus.NOTIFIED);
            log.info("Notified waiting list entry id={} for newly released slot", next.getWaitingId());
        }

        auditService.log(currentUserId, "CANCEL_APPOINTMENT", "APPOINTMENT",
                String.valueOf(appointmentId), "Cancelled appointment " + appointment.getAppointmentReference() + ". Reason: " + reason);

        return getAppointmentById(appointmentId);
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        appointmentRepository.updateStatus(appointmentId, newStatus);

        auditService.log(currentUserId, "UPDATE_STATUS", "APPOINTMENT",
                String.valueOf(appointmentId), "Updated status to " + newStatus.name());

        return getAppointmentById(appointmentId);
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    @Override
    public Appointment getAppointmentByReference(String reference) {
        return appointmentRepository.findByReference(reference)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with reference: " + reference));
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getAppointmentsForDoctorAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorAndDate(doctorId, date);
    }

    @Override
    public List<Appointment> getAppointmentsForToday() {
        return appointmentRepository.findByDate(LocalDate.now());
    }

    @Override
    public List<Appointment> getUpcomingAppointments(int limit) {
        return appointmentRepository.findUpcomingAppointments(limit);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}