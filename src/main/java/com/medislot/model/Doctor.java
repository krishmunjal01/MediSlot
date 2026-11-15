package com.medislot.model;

import com.medislot.model.enums.DoctorStatus;
import com.medislot.model.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Demonstrates Inheritance and Encapsulation:
 * Extends base User and encapsulates clinical specialization, consultation fee, and department data.
 */
public class Doctor extends User {
    private Long doctorId;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private String qualification;
    private int experienceYears;
    private BigDecimal consultationFee;
    private DoctorStatus status;

    public Doctor() {
        super();
        setRole(Role.DOCTOR);
        this.status = DoctorStatus.ACTIVE;
        this.consultationFee = BigDecimal.valueOf(500.00);
    }

    public Doctor(Long userId, String username, String email, String passwordHash,
                  String fullName, String phone, boolean active, LocalDateTime createdAt,
                  Long doctorId, Long departmentId, String departmentName, String specialization,
                  String qualification, int experienceYears, BigDecimal consultationFee, DoctorStatus status) {
        super(userId, username, email, passwordHash, Role.DOCTOR, fullName, phone, active, createdAt);
        this.doctorId = doctorId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee != null ? consultationFee : BigDecimal.valueOf(500.00);
        this.status = status != null ? status : DoctorStatus.ACTIVE;
    }

    @Override
    public String getDashboardUrl() {
        return "/doctor/dashboard";
    }

    @Override
    public String getRoleDescription() {
        return "Attending Medical Specialist in " + (specialization != null ? specialization : "General Medicine");
    }

    // Getters and Setters
    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public DoctorStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorStatus status) {
        this.status = status;
    }
}
