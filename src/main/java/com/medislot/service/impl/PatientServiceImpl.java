package com.medislot.service.impl;

import com.medislot.dto.PatientRegistrationDto;
import com.medislot.exception.PatientNotFoundException;
import com.medislot.exception.ValidationException;
import com.medislot.model.Patient;
import com.medislot.model.User;
import com.medislot.model.enums.Role;
import com.medislot.repository.PatientRepository;
import com.medislot.repository.UserRepository;
import com.medislot.service.AuditService;
import com.medislot.service.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public PatientServiceImpl(PatientRepository patientRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              AuditService auditService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Patient registerPatient(PatientRegistrationDto dto, Long currentUserId) {
        // Validate uniqueness of email and phone
        if (patientRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("A patient with email " + dto.getEmail() + " is already registered.");
        }
        if (patientRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new ValidationException("A patient with phone " + dto.getPhone() + " is already registered.");
        }

        Long userId = null;
        if (dto.isCreateAccount() || (dto.getUsername() != null && !dto.getUsername().trim().isEmpty())) {
            String username = dto.getUsername() != null && !dto.getUsername().trim().isEmpty() ?
                    dto.getUsername().trim() : dto.getEmail().split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);

            if (userRepository.findByUsername(username).isPresent()) {
                throw new ValidationException("Username '" + username + "' is already taken.");
            }

            String rawPassword = dto.getPassword() != null && !dto.getPassword().trim().isEmpty() ?
                    dto.getPassword() : "Patient@123";

            Patient userAccount = new Patient();
            userAccount.setUsername(username);
            userAccount.setEmail(dto.getEmail());
            userAccount.setPasswordHash(passwordEncoder.encode(rawPassword));
            userAccount.setRole(Role.PATIENT);
            userAccount.setFullName(dto.getFullName());
            userAccount.setPhone(dto.getPhone());
            userAccount.setActive(true);
            userAccount.setCreatedAt(LocalDateTime.now());

            userId = userRepository.save(userAccount);
        }

        // Generate Unique Patient Code
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String patientCode = "PAT-" + datePrefix + "-" + randomSuffix;

        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setPatientCode(patientCode);
        patient.setFullName(dto.getFullName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setRegistrationDate(LocalDateTime.now());

        Long patientId = patientRepository.save(patient);
        patient.setPatientId(patientId);

        auditService.log(currentUserId, "REGISTER_PATIENT", "PATIENT",
                String.valueOf(patientId), "Registered patient " + patient.getFullName() + " with code " + patientCode);

        return patient;
    }

    @Override
    @Transactional
    public Patient updatePatient(PatientRegistrationDto dto, Long currentUserId) {
        Patient existing = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));

        existing.setFullName(dto.getFullName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setGender(dto.getGender());
        existing.setBloodGroup(dto.getBloodGroup());
        existing.setAddress(dto.getAddress());
        existing.setEmergencyContact(dto.getEmergencyContact());

        patientRepository.update(existing);

        if (existing.getUserId() != null) {
            userRepository.findById(existing.getUserId()).ifPresent(user -> {
                user.setFullName(dto.getFullName());
                user.setEmail(dto.getEmail());
                user.setPhone(dto.getPhone());
                userRepository.update(user);
            });
        }

        auditService.log(currentUserId, "UPDATE_PATIENT", "PATIENT",
                String.valueOf(existing.getPatientId()), "Updated demographic details for " + existing.getFullName());

        return existing;
    }

    @Override
    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    @Override
    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("Patient profile not found for user ID: " + userId));
    }

    @Override
    public Patient getPatientByCode(String code) {
        return patientRepository.findByPatientCode(code)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with code: " + code));
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public List<Patient> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword);
    }

    @Override
    public long getTotalPatientCount() {
        return patientRepository.countAllPatients();
    }
}

