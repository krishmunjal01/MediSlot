package com.medislot.repository;

import com.medislot.model.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository {
    Optional<Doctor> findById(Long doctorId);
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findAll();
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> searchDoctors(String query, Long departmentId);
    Long save(Doctor doctor);
    int update(Doctor doctor);
    int updateStatus(Long doctorId, String status);
    long countActiveDoctors();
}

