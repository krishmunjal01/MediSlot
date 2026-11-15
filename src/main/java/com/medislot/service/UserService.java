package com.medislot.service;

import com.medislot.model.User;
import com.medislot.model.enums.Role;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> getUsersByRole(Role role);
    List<User> getAllUsers();
    void updatePassword(Long userId, String rawNewPassword);
}
