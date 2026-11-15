package com.medislot.service;

import com.medislot.model.Department;
import java.util.List;

public interface DepartmentService {
    Department getDepartmentById(Long departmentId);
    List<Department> getAllDepartments();
    Department saveDepartment(Department department, Long currentUserId);
    void deleteDepartment(Long departmentId, Long currentUserId);
}
