package com.medislot.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class WaitingListRequest {
    @NotNull(message = "Doctor is required")
    private Long doctorId;

    @NotNull(message = "Patient is required")
    private Long patientId;

    @NotNull(message = "Requested date is required")
    private LocalDate requestedDate;

    private String notes;

    public WaitingListRequest() {
    }

    public WaitingListRequest(Long doctorId, Long patientId, LocalDate requestedDate, String notes) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.requestedDate = requestedDate;
        this.notes = notes;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
