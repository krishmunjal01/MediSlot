package com.medislot.model;

import com.medislot.model.enums.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Demonstrates Inheritance and Domain Encapsulation:
 * Extends User, adding medical demographic data and domain methods like calculateAge().
 */
public class Patient extends User {
    private Long patientId;
    private String patientCode;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private LocalDateTime registrationDate;

    public Patient() {
        super();
        setRole(Role.PATIENT);
        this.registrationDate = LocalDateTime.now();
    }

    public Patient(Long userId, String username, String email, String passwordHash,
                   String fullName, String phone, boolean active, LocalDateTime createdAt,
                   Long patientId, String patientCode, LocalDate dateOfBirth, String gender,
                   String bloodGroup, String address, String emergencyContact, LocalDateTime registrationDate) {
        super(userId, username, email, passwordHash, Role.PATIENT, fullName, phone, active, createdAt);
        this.patientId = patientId;
        this.patientCode = patientCode;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDateTime.now();
    }

    @Override
    public String getDashboardUrl() {
        return "/patient/dashboard";
    }

    @Override
    public String getRoleDescription() {
        return "Registered Hospital Patient (ID: " + (patientCode != null ? patientCode : "Pending") + ")";
    }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
}
