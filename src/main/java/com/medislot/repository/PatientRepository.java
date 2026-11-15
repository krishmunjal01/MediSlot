package com.medislot.repository;

import com.medislot.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    Optional<Patient> findById(Long patientId);
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByPatientCode(String patientCode);
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByPhone(String phone);
    List<Patient> findAll();
    List<Patient> searchPatients(String keyword);
    Long save(Patient patient);
    int update(Patient patient);
    long countAllPatients();
}
