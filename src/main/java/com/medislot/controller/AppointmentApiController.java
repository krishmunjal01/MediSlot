package com.medislot.controller;

import com.medislot.model.AppointmentSlot;
import com.medislot.service.DoctorScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class AppointmentApiController {

    private final DoctorScheduleService scheduleService;

    public AppointmentApiController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Retrieves available time slots dynamically for a doctor on a specific date.
     */
    @GetMapping("/slots")
    public ResponseEntity<List<AppointmentSlot>> getAvailableSlots(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AppointmentSlot> slots = scheduleService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }
}
