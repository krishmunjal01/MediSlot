package com.medislot.repository;

import com.medislot.model.AppointmentSlot;
import com.medislot.model.enums.SlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepository {
    Optional<AppointmentSlot> findById(Long slotId);

    /**
     * Critical for Concurrency: Acquires a pessimistic row-level lock using SELECT FOR UPDATE
     */
    Optional<AppointmentSlot> findByIdForUpdate(Long slotId);

    List<AppointmentSlot> findSlotsByDoctorAndDate(Long doctorId, LocalDate date);
    List<AppointmentSlot> findAvailableSlotsByDoctorAndDate(Long doctorId, LocalDate date);
    List<AppointmentSlot> findAvailableSlotsByDepartmentAndDate(Long departmentId, LocalDate date);
    boolean existsByDoctorDateAndTime(Long doctorId, LocalDate date, LocalTime startTime);

    Long save(AppointmentSlot slot);
    int batchInsertSlots(List<AppointmentSlot> slots);
    int updateStatus(Long slotId, SlotStatus status);
    int deleteUnbookedSlotsByDoctorAndDate(Long doctorId, LocalDate date);
}