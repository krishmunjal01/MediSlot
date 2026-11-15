package com.medislot.service.impl;

import com.medislot.dto.DashboardStatsDto;
import com.medislot.repository.AppointmentRepository;
import com.medislot.repository.DoctorRepository;
import com.medislot.repository.PatientRepository;
import com.medislot.repository.WaitingListRepository;
import com.medislot.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final WaitingListRepository waitingListRepository;

    public DashboardServiceImpl(AppointmentRepository appointmentRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository,
                                WaitingListRepository waitingListRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.waitingListRepository = waitingListRepository;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        long todayCount = appointmentRepository.countAppointmentsByDate(LocalDate.now());
        long upcomingCount = appointmentRepository.countUpcomingAppointments();
        long completedCount = appointmentRepository.countCompletedAppointments();
        long cancelledCount = appointmentRepository.countCancelledAppointments();
        long waitingCount = waitingListRepository.countWaitingEntries();
        long patientCount = patientRepository.countAllPatients();
        long activeDoctorCount = doctorRepository.countActiveDoctors();

        return new DashboardStatsDto(todayCount, upcomingCount, completedCount, cancelledCount,
                waitingCount, patientCount, activeDoctorCount);
    }

    @Override
    public Map<String, Long> getAppointmentStatusDistribution() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    @Override
    public List<Map<String, Object>> getDepartmentDistribution() {
        return appointmentRepository.getDepartmentAppointmentDistribution();
    }

    @Override
    public List<Map<String, Object>> getDoctorPerformanceSummary() {
        return appointmentRepository.getDoctorAppointmentSummary();
    }
}

