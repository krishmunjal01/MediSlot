package com.medislot.model;

import java.time.LocalDateTime;

public class Department {
    private Long departmentId;
    private String name;
    private String code;
    private String description;
    private String headDoctorName;
    private LocalDateTime createdAt;

    public Department() {
        this.createdAt = LocalDateTime.now();
    }

    public Department(Long departmentId, String name, String code, String description,
                      String headDoctorName, LocalDateTime createdAt) {
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.headDoctorName = headDoctorName;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadDoctorName() {
        return headDoctorName;
    }

    public void setHeadDoctorName(String headDoctorName) {
        this.headDoctorName = headDoctorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
