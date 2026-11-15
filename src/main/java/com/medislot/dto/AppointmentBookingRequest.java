package com.medislot.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AppointmentBookingRequest {
    @NotNull(message = "Patient selection is required")
    private Long patientId;

    @NotNull(message = "Doctor selection is required")
    private Long doctorId;

    @NotNull(message = "Slot selection is required")
    private Long slotId;

    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;

    private String reasonForVisit;

    public AppointmentBookingRequest() {
    }

    public AppointmentBookingRequest(Long patientId, Long doctorId, Long slotId, LocalDate appointmentDate, String reasonForVisit) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.appointmentDate = appointmentDate;
        this.reasonForVisit = reasonForVisit;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }
}
