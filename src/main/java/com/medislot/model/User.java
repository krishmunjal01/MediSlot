package com.medislot.model;

import com.medislot.model.enums.Role;
import java.time.LocalDateTime;

/**
 * Demonstrates Object-Oriented Programming:
 * - Abstraction: Abstract base class representing generic system users
 * - Encapsulation: Private members with public accessors and mutators
 * - Polymorphism: Polymorphic methods implemented differently by subclasses
 */
public abstract class User {
    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private String fullName;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;

    // Default constructor
    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Parameterized constructor
    public User(Long userId, String username, String email, String passwordHash, Role role,
                String fullName, String phone, boolean active, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Abstract & Polymorphic methods
    public abstract String getDashboardUrl();
    public abstract String getRoleDescription();

    public boolean hasAdministrativePrivileges() {
        return this.role == Role.ADMIN;
    }

    // Getters and Setters (Encapsulation)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}

