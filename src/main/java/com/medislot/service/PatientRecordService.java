package com.medislot.service;

import com.medislot.dto.ConsultationRecordDto;
import com.medislot.model.PatientRecord;
import java.util.List;

public interface PatientRecordService {
    PatientRecord createRecord(ConsultationRecordDto dto, Long currentUserId);
    PatientRecord updateRecord(ConsultationRecordDto dto, Long currentUserId);
    PatientRecord getRecordByAppointmentId(Long appointmentId);
    List<PatientRecord> getRecordsForPatient(Long patientId);
    List<PatientRecord> getRecordsForDoctor(Long doctorId);
}
