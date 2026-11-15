package com.medislot.exception;

public class AppointmentNotFoundException extends ResourceNotFoundException {
    public AppointmentNotFoundException(String message) {
        super(message);
    }

    public AppointmentNotFoundException(Long appointmentId) {
        super("Appointment not found with ID: " + appointmentId);
    }
}
