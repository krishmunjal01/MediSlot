package com.medislot.model;

import com.medislot.model.enums.WaitingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitingListEntry {
    private Long waitingId;
    private Long doctorId;
    private Long patientId;
    private LocalDate requestedDate;
    private int queuePosition;
    private WaitingStatus status;
    private String notes;
    private LocalDateTime createdAt;

    // Display fields
    private String doctorName;
    private String patientName;
    private String patientPhone;

    public WaitingListEntry() {
        this.status = WaitingStatus.WAITING;
        this.queuePosition = 1;
        this.createdAt = LocalDateTime.now();
    }

    public WaitingListEntry(Long waitingId, Long doctorId, Long patientId,
                            LocalDate requestedDate, int queuePosition, WaitingStatus status,
                            String notes, LocalDateTime createdAt) {
        this.waitingId = waitingId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.requestedDate = requestedDate;
        this.queuePosition = queuePosition;
        this.status = status != null ? status : WaitingStatus.WAITING;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getWaitingId() {
        return waitingId;
    }

    public void setWaitingId(Long waitingId) {
        this.waitingId = waitingId;
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

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public WaitingStatus getStatus() {
        return status;
    }

    public void setStatus(WaitingStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }
}
