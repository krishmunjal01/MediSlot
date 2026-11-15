package com.medislot.service.impl;

import com.medislot.dto.ConsultationRecordDto;
import com.medislot.exception.ResourceNotFoundException;
import com.medislot.model.PatientRecord;
import com.medislot.model.enums.AppointmentStatus;
import com.medislot.repository.AppointmentRepository;
import com.medislot.repository.PatientRecordRepository;
import com.medislot.service.AuditService;
import com.medislot.service.PatientRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientRecordServiceImpl implements PatientRecordService {

    private final PatientRecordRepository recordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    public PatientRecordServiceImpl(PatientRecordRepository recordRepository,
                                  AppointmentRepository appointmentRepository,
                                  AuditService auditService) {
        this.recordRepository = recordRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public PatientRecord createRecord(ConsultationRecordDto dto, Long currentUserId) {
        PatientRecord record = new PatientRecord();
        record.setAppointmentId(dto.getAppointmentId());
        record.setPatientId(dto.getPatientId());
        record.setDoctorId(dto.getDoctorId());
        record.setSymptoms(dto.getSymptoms());
        record.setDiagnosis(dto.getDiagnosis());
        record.setPrescription(dto.getPrescription());
        record.setClinicalNotes(dto.getClinicalNotes());
        record.setFollowUpDate(dto.getFollowUpDate());
        record.setCreatedAt(LocalDateTime.now());

        Long recordId = recordRepository.save(record);
        record.setRecordId(recordId);

        // Mark appointment as COMPLETED
        appointmentRepository.updateStatus(dto.getAppointmentId(), AppointmentStatus.COMPLETED);

        auditService.log(currentUserId, "ADD_PATIENT_RECORD", "PATIENT_RECORD",
                String.valueOf(recordId), "Created clinical consultation record for appointment id=" + dto.getAppointmentId());

        return record;
    }

    @Override
    @Transactional
    public PatientRecord updateRecord(ConsultationRecordDto dto, Long currentUserId) {
        PatientRecord record = recordRepository.findById(dto.getRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient record not found."));

        record.setSymptoms(dto.getSymptoms());
        record.setDiagnosis(dto.getDiagnosis());
        record.setPrescription(dto.getPrescription());
        record.setClinicalNotes(dto.getClinicalNotes());
        record.setFollowUpDate(dto.getFollowUpDate());

        recordRepository.update(record);

        auditService.log(currentUserId, "UPDATE_PATIENT_RECORD", "PATIENT_RECORD",
                String.valueOf(record.getRecordId()), "Updated medical record id=" + record.getRecordId());

        return record;
    }

    @Override
    public PatientRecord getRecordByAppointmentId(Long appointmentId) {
        return recordRepository.findByAppointmentId(appointmentId).orElse(null);
    }

    @Override
    public List<PatientRecord> getRecordsForPatient(Long patientId) {
        return recordRepository.findByPatientId(patientId);
    }

    @Override
    public List<PatientRecord> getRecordsForDoctor(Long doctorId) {
        return recordRepository.findByDoctorId(doctorId);
    }
}
