package com.medislot.service.impl;

import com.medislot.exception.ResourceNotFoundException;
import com.medislot.model.Department;
import com.medislot.repository.DepartmentRepository;
import com.medislot.service.AuditService;
import com.medislot.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Override
    public Department getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @Transactional
    public Department saveDepartment(Department department, Long currentUserId) {
        if (department.getDepartmentId() != null) {
            departmentRepository.update(department);
            auditService.log(currentUserId, "UPDATE_DEPT", "DEPARTMENT",
                    String.valueOf(department.getDepartmentId()), "Updated department: " + department.getName());
            return department;
        } else {
            department.setCreatedAt(LocalDateTime.now());
            Long id = departmentRepository.save(department);
            department.setDepartmentId(id);
            auditService.log(currentUserId, "CREATE_DEPT", "DEPARTMENT",
                    String.valueOf(id), "Created department: " + department.getName());
            return department;
        }
    }

    @Override
    @Transactional
    public void deleteDepartment(Long departmentId, Long currentUserId) {
        departmentRepository.deleteById(departmentId);
        auditService.log(currentUserId, "DELETE_DEPT", "DEPARTMENT",
                String.valueOf(departmentId), "Deleted department ID: " + departmentId);
    }
}
