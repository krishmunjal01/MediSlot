package com.medislot.repository;

import com.medislot.model.User;
import com.medislot.model.enums.Role;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findAll();
    Long save(User user);
    int update(User user);
    int updatePassword(Long userId, String passwordHash);
    int deleteById(Long userId);
}

