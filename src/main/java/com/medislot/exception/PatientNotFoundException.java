package com.medislot.exception;

public class PatientNotFoundException extends ResourceNotFoundException {
    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(Long patientId) {
        super("Patient not found with ID: " + patientId);
    }
}
