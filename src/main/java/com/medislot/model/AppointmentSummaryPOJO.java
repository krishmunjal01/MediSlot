package com.medislot.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Academic Demonstration Component:
 * This class is a Plain Old Java Object (POJO).
 *
 * Characteristics:
 * 1. Independent of Spring framework annotations (@Component, @Service, @Autowired).
 * 2. Instantiated explicitly by application code using the 'new' keyword:
 *    AppointmentSummaryPOJO summary = new AppointmentSummaryPOJO(...);
 * 3. Lifecycle managed by the Java Garbage Collector (JVM heap allocation).
 * 4. Contains pure domain data and encapsulation logic without framework overhead.
 */
public class AppointmentSummaryPOJO {
    private String reference;
    private String patientName;
    private String doctorName;
    private String department;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private double consultationFee;

    // Default Constructor
    public AppointmentSummaryPOJO() {
    }

    // Parameterized Constructor
    public AppointmentSummaryPOJO(String reference, String patientName, String doctorName,
                                 String department, LocalDate appointmentDate,
                                 LocalTime appointmentTime, String status, double consultationFee) {
        this.reference = reference;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.consultationFee = consultationFee;
    }

    // Domain business method encapsulated in POJO
    public String getFormattedSummary() {
        return String.format("[%s] %s with Dr. %s (%s) on %s at %s - Status: %s - Fee: $%.2f",
                reference, patientName, doctorName, department, appointmentDate, appointmentTime, status, consultationFee);
    }

    // Getters and Setters
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }
}
