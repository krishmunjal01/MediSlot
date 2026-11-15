package com.medislot.service;

import com.medislot.dto.DoctorFormDto;
import com.medislot.model.Doctor;
import java.util.List;

public interface DoctorService {
    Doctor createDoctor(DoctorFormDto dto, Long currentUserId);
    Doctor updateDoctor(DoctorFormDto dto, Long currentUserId);
    Doctor getDoctorById(Long doctorId);
    Doctor getDoctorByUserId(Long userId);
    List<Doctor> getAllDoctors();
    List<Doctor> getDoctorsByDepartment(Long departmentId);
    List<Doctor> searchDoctors(String query, Long departmentId);
    void updateDoctorStatus(Long doctorId, String status, Long currentUserId);
    long getActiveDoctorCount();
}
