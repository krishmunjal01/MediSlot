package com.medislot.service.impl;

import com.medislot.dto.ScheduleFormDto;
import com.medislot.exception.ValidationException;
import com.medislot.model.AppointmentSlot;
import com.medislot.model.DoctorSchedule;
import com.medislot.model.enums.SlotStatus;
import com.medislot.repository.AppointmentSlotRepository;
import com.medislot.repository.DoctorScheduleRepository;
import com.medislot.service.AuditService;
import com.medislot.service.DoctorScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private static final Logger log = LoggerFactory.getLogger(DoctorScheduleServiceImpl.class);

    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentSlotRepository slotRepository;
    private final AuditService auditService;

    public DoctorScheduleServiceImpl(DoctorScheduleRepository scheduleRepository,
                                     AppointmentSlotRepository slotRepository,
                                     AuditService auditService) {
        this.scheduleRepository = scheduleRepository;
        this.slotRepository = slotRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public DoctorSchedule saveSchedule(ScheduleFormDto dto, Long currentUserId) {
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new ValidationException("Consultation start time must be before end time.");
        }

        DayOfWeek day = DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase());

        // Check for existing schedule on same day
        scheduleRepository.findByDoctorAndDay(dto.getDoctorId(), day).ifPresent(existing -> {
            if (dto.getScheduleId() == null || !existing.getScheduleId().equals(dto.getScheduleId())) {
                throw new ValidationException("A schedule already exists for this doctor on " + day + ".");
            }
        });

        DoctorSchedule schedule;
        if (dto.getScheduleId() != null) {
            schedule = scheduleRepository.findById(dto.getScheduleId())
                    .orElseThrow(() -> new ValidationException("Schedule not found."));
            schedule.setDayOfWeek(day);
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            schedule.setSlotDurationMinutes(dto.getSlotDurationMinutes());
            schedule.setActive(dto.isActive());
            scheduleRepository.update(schedule);
        } else {
            schedule = new DoctorSchedule(null, dto.getDoctorId(), day,
                    dto.getStartTime(), dto.getEndTime(), dto.getSlotDurationMinutes(), dto.isActive());
            Long id = scheduleRepository.save(schedule);
            schedule.setScheduleId(id);
        }

        if (dto.isGenerateSlotsImmediately()) {
            int daysAhead = dto.getWeeksAhead() > 0 ? dto.getWeeksAhead() * 7 : 14;
            generateSlotsForDoctor(dto.getDoctorId(), LocalDate.now(), daysAhead);
        }

        auditService.log(currentUserId, "SAVE_SCHEDULE", "SCHEDULE",
                String.valueOf(schedule.getScheduleId()), "Saved schedule for doctor id=" + dto.getDoctorId() + " on " + day);

        return schedule;
    }

    @Override
    public List<DoctorSchedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<DoctorSchedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, Long currentUserId) {
        scheduleRepository.deleteById(scheduleId);
        auditService.log(currentUserId, "DELETE_SCHEDULE", "SCHEDULE",
                String.valueOf(scheduleId), "Deleted schedule ID " + scheduleId);
    }

    /**
     * Generates discrete consultation slots from doctor's active weekly schedule.
     */
    @Override
    @Transactional
    public int generateSlotsForDoctor(Long doctorId, LocalDate startDate, int daysAhead) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        if (schedules.isEmpty()) {
            return 0;
        }

        List<AppointmentSlot> newSlots = new ArrayList<>();

        for (int i = 0; i < daysAhead; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DayOfWeek currentDay = currentDate.getDayOfWeek();

            for (DoctorSchedule sched : schedules) {
                if (sched.isActive() && sched.getDayOfWeek() == currentDay) {
                    LocalTime currentSlotTime = sched.getStartTime();
                    LocalTime schedEndTime = sched.getEndTime();
                    int duration = sched.getSlotDurationMinutes();

                    while (currentSlotTime.plusMinutes(duration).compareTo(schedEndTime) <= 0) {
                        LocalTime slotEnd = currentSlotTime.plusMinutes(duration);

                        AppointmentSlot slot = new AppointmentSlot();
                        slot.setDoctorId(doctorId);
                        slot.setSlotDate(currentDate);
                        slot.setStartTime(currentSlotTime);
                        slot.setEndTime(slotEnd);
                        slot.setStatus(SlotStatus.AVAILABLE);

                        newSlots.add(slot);
                        currentSlotTime = slotEnd;
                    }
                }
            }
        }

        if (!newSlots.isEmpty()) {
            int count = slotRepository.batchInsertSlots(newSlots);
            log.info("Generated {} slots for doctorId={} starting from {}", newSlots.size(), doctorId, startDate);
            return count;
        }

        return 0;
    }

    @Override
    public List<AppointmentSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        // Automatically ensure slots exist for this day if schedule exists
        if (slotRepository.findSlotsByDoctorAndDate(doctorId, date).isEmpty()) {
            generateSlotsForDoctor(doctorId, date, 1);
        }
        return slotRepository.findAvailableSlotsByDoctorAndDate(doctorId, date);
    }

    @Override
    public List<AppointmentSlot> getAllSlotsForDoctorAndDate(Long doctorId, LocalDate date) {
        return slotRepository.findSlotsByDoctorAndDate(doctorId, date);
    }

    @Override
    public List<AppointmentSlot> getAvailableSlotsByDepartment(Long departmentId, LocalDate date) {
        return slotRepository.findAvailableSlotsByDepartmentAndDate(departmentId, date);
    }
}