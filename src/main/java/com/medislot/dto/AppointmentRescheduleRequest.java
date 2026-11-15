package com.medislot.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AppointmentRescheduleRequest {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "New slot ID is required")
    private Long newSlotId;

    @NotNull(message = "New date is required")
    private LocalDate newDate;

    private String reason;

    public AppointmentRescheduleRequest() {
    }

    public AppointmentRescheduleRequest(Long appointmentId, Long newSlotId, LocalDate newDate, String reason) {
        this.appointmentId = appointmentId;
        this.newSlotId = newSlotId;
        this.newDate = newDate;
        this.reason = reason;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getNewSlotId() {
        return newSlotId;
    }

    public void setNewSlotId(Long newSlotId) {
        this.newSlotId = newSlotId;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
