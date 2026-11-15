package com.medislot.exception;

public class DoctorNotFoundException extends ResourceNotFoundException {
    public DoctorNotFoundException(String message) {
        super(message);
    }

    public DoctorNotFoundException(Long doctorId) {
        super("Doctor not found with ID: " + doctorId);
    }
}

