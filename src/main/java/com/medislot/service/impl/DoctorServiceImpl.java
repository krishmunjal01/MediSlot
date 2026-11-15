package com.medislot.service.impl;

import com.medislot.dto.DoctorFormDto;
import com.medislot.exception.DoctorNotFoundException;
import com.medislot.exception.ValidationException;
import com.medislot.model.Doctor;
import com.medislot.model.enums.DoctorStatus;
import com.medislot.model.enums.Role;
import com.medislot.repository.DoctorRepository;
import com.medislot.repository.UserRepository;
import com.medislot.service.AuditService;
import com.medislot.service.DoctorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             AuditService auditService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Doctor createDoctor(DoctorFormDto dto, Long currentUserId) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ValidationException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Email '" + dto.getEmail() + "' is already in use.");
        }

        Doctor doctorUser = new Doctor();
        doctorUser.setUsername(dto.getUsername());
        doctorUser.setEmail(dto.getEmail());
        doctorUser.setPasswordHash(passwordEncoder.encode(dto.getPassword() != null && !dto.getPassword().trim().isEmpty() ?
                dto.getPassword() : "Doctor@123"));
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setFullName(dto.getFullName());
        doctorUser.setPhone(dto.getPhone());
        doctorUser.setActive(true);
        doctorUser.setCreatedAt(LocalDateTime.now());

        Long userId = userRepository.save(doctorUser);

        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setQualification(dto.getQualification());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setStatus(dto.getStatus() != null ? DoctorStatus.valueOf(dto.getStatus()) : DoctorStatus.ACTIVE);

        Long doctorId = doctorRepository.save(doctor);
        doctor.setDoctorId(doctorId);

        auditService.log(currentUserId, "CREATE_DOCTOR", "DOCTOR",
                String.valueOf(doctorId), "Added new doctor Dr. " + dto.getFullName());

        return getDoctorById(doctorId);
    }

    @Override
    @Transactional
    public Doctor updateDoctor(DoctorFormDto dto, Long currentUserId) {
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(dto.getDoctorId()));

        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setQualification(dto.getQualification());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        if (dto.getStatus() != null) {
            doctor.setStatus(DoctorStatus.valueOf(dto.getStatus()));
        }

        doctorRepository.update(doctor);

        // Update corresponding User record
        userRepository.findById(doctor.getUserId()).ifPresent(user -> {
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            userRepository.update(user);
        });

        auditService.log(currentUserId, "UPDATE_DOCTOR", "DOCTOR",
                String.valueOf(doctor.getDoctorId()), "Updated doctor details for Dr. " + dto.getFullName());

        return getDoctorById(doctor.getDoctorId());
    }

    @Override
    public Doctor getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
    }

    @Override
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for user ID: " + userId));
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Doctor> searchDoctors(String query, Long departmentId) {
        return doctorRepository.searchDoctors(query, departmentId);
    }

    @Override
    @Transactional
    public void updateDoctorStatus(Long doctorId, String status, Long currentUserId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(doctorId));

        doctorRepository.updateStatus(doctorId, status);
        auditService.log(currentUserId, "CHANGE_DOCTOR_STATUS", "DOCTOR",
                String.valueOf(doctorId), "Changed Dr. " + doctor.getFullName() + " status to " + status);
    }

    @Override
    public long getActiveDoctorCount() {
        return doctorRepository.countActiveDoctors();
    }
}
