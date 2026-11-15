package com.medislot.repository;

import com.medislot.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    Optional<Department> findById(Long departmentId);
    Optional<Department> findByCode(String code);
    List<Department> findAll();
    Long save(Department department);
    int update(Department department);
    int deleteById(Long departmentId);
}
