package com.medislot.service;

import com.medislot.dto.ScheduleFormDto;
import com.medislot.model.AppointmentSlot;
import com.medislot.model.DoctorSchedule;
import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {
    DoctorSchedule saveSchedule(ScheduleFormDto dto, Long currentUserId);
    List<DoctorSchedule> getSchedulesByDoctor(Long doctorId);
    List<DoctorSchedule> getAllSchedules();
    void deleteSchedule(Long scheduleId, Long currentUserId);

    int generateSlotsForDoctor(Long doctorId, LocalDate startDate, int daysAhead);
    List<AppointmentSlot> getAvailableSlots(Long doctorId, LocalDate date);
    List<AppointmentSlot> getAllSlotsForDoctorAndDate(Long doctorId, LocalDate date);
    List<AppointmentSlot> getAvailableSlotsByDepartment(Long departmentId, LocalDate date);
}
