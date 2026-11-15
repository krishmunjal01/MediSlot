package com.medislot.service;

import com.medislot.dto.PatientRegistrationDto;
import com.medislot.model.Patient;
import java.util.List;

public interface PatientService {
    Patient registerPatient(PatientRegistrationDto dto, Long currentUserId);
    Patient updatePatient(PatientRegistrationDto dto, Long currentUserId);
    Patient getPatientById(Long patientId);
    Patient getPatientByUserId(Long userId);
    Patient getPatientByCode(String code);
    List<Patient> getAllPatients();
    List<Patient> searchPatients(String keyword);
    long getTotalPatientCount();
}

