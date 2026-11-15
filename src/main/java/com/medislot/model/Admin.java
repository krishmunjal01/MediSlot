package com.medislot.model;

import com.medislot.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Demonstrates Inheritance: Admin specializes User with administrative capabilities.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(Long userId, String username, String email, String passwordHash,
                 String fullName, String phone, boolean active, LocalDateTime createdAt) {
        super(userId, username, email, passwordHash, Role.ADMIN, fullName, phone, active, createdAt);
    }

    @Override
    public String getDashboardUrl() {
        return "/admin/dashboard";
    }

    @Override
    public String getRoleDescription() {
        return "Hospital Operations Executive with system-wide oversight and management privileges.";
    }

    @Override
    public boolean hasAdministrativePrivileges() {
        return true;
    }
}

