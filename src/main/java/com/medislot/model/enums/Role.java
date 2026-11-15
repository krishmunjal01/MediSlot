package com.medislot.model.enums;

public enum Role {
    ADMIN("Administrator"),
    DOCTOR("Medical Doctor"),
    RECEPTIONIST("Front Desk Receptionist"),
    PATIENT("Registered Patient");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
