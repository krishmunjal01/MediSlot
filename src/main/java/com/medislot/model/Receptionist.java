package com.medislot.model;

import com.medislot.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Demonstrates Inheritance: Receptionist specializes User with front desk operational responsibilities.
 */
public class Receptionist extends User {

    public Receptionist() {
        super();
        setRole(Role.RECEPTIONIST);
    }

    public Receptionist(Long userId, String username, String email, String passwordHash,
                        String fullName, String phone, boolean active, LocalDateTime createdAt) {
        super(userId, username, email, passwordHash, Role.RECEPTIONIST, fullName, phone, active, createdAt);
    }

    @Override
    public String getDashboardUrl() {
        return "/receptionist/dashboard";
    }

    @Override
    public String getRoleDescription() {
        return "Patient Reception and Clinic Front Desk Coordinator.";
    }
}
