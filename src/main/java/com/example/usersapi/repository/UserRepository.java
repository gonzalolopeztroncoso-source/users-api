package com.example.usersapi.repository;

import com.example.usersapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}