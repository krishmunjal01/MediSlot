package com.medislot.model.enums;

public enum SlotStatus {
    AVAILABLE("Available"),
    LOCKED("Locked"),
    BOOKED("Booked"),
    CANCELLED("Cancelled");

    private final String label;

    SlotStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

