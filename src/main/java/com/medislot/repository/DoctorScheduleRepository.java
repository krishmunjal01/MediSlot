package com.medislot.repository;

import com.medislot.model.DoctorSchedule;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository {
    Optional<DoctorSchedule> findById(Long scheduleId);
    List<DoctorSchedule> findByDoctorId(Long doctorId);
    Optional<DoctorSchedule> findByDoctorAndDay(Long doctorId, DayOfWeek dayOfWeek);
    List<DoctorSchedule> findAll();
    Long save(DoctorSchedule schedule);
    int update(DoctorSchedule schedule);
    int deleteById(Long scheduleId);
}
