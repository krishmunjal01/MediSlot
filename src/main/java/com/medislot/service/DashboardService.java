package com.medislot.service;

import com.medislot.dto.DashboardStatsDto;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardStatsDto getDashboardStats();
    Map<String, Long> getAppointmentStatusDistribution();
    List<Map<String, Object>> getDepartmentDistribution();
    List<Map<String, Object>> getDoctorPerformanceSummary();
}
