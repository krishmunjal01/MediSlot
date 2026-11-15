package com.medislot.model.enums;

public enum WaitingStatus {
    WAITING("Waiting in Queue"),
    NOTIFIED("Slot Available / Notified"),
    CONVERTED("Converted to Appointment"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");

    private final String label;

    WaitingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
