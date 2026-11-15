package com.medislot.model.enums;

public enum AppointmentStatus {
    BOOKED("Booked", "#476B56", "#E8EFE8"),
    CONFIRMED("Confirmed", "#355E3B", "#DCEAD8"),
    CHECKED_IN("Checked In", "#2B547E", "#E3EDF7"),
    COMPLETED("Completed", "#292D29", "#EFEFEF"),
    CANCELLED("Cancelled", "#A9564C", "#FBEBEA"),
    NO_SHOW("No Show", "#A77B35", "#F8F3EA");

    private final String label;
    private final String textColor;
    private final String bgColor;

    AppointmentStatus(String label, String textColor, String bgColor) {
        this.label = label;
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    public String getLabel() {
        return label;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    public boolean canBeRescheduled() {
        return this == BOOKED || this == CONFIRMED;
    }

    public boolean canBeCancelled() {
        return this == BOOKED || this == CONFIRMED;
    }

    public boolean canBeCheckedIn() {
        return this == BOOKED || this == CONFIRMED;
    }

    public boolean canBeCompleted() {
        return this == CHECKED_IN || this == CONFIRMED || this == BOOKED;
    }
}

