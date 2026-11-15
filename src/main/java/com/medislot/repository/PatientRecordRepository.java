package com.medislot.repository;

import com.medislot.model.PatientRecord;
import java.util.List;
import java.util.Optional;

public interface PatientRecordRepository {
    Optional<PatientRecord> findById(Long recordId);
    Optional<PatientRecord> findByAppointmentId(Long appointmentId);
    List<PatientRecord> findByPatientId(Long patientId);
    List<PatientRecord> findByDoctorId(Long doctorId);
    Long save(PatientRecord record);
    int update(PatientRecord record);
}
