package com.medislot.model.enums;

public enum DoctorStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ON_LEAVE("On Leave");

    private final String label;

    DoctorStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

