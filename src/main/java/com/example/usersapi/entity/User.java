package com.example.usersapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(length = 36, nullable = false)
    private String id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 120)
    private String email;
    @Column(name = "password", length = 100, nullable = false)
    private String password;
    @Column(nullable = false)
    private LocalDateTime created;
    @Column(nullable = false)
    private LocalDateTime modified;
    @Column(name = "last_login", nullable = false)
    private LocalDateTime lastLogin;
    @Column(nullable = false, length = 255)
    private String token;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    private List<Phone> phones = new ArrayList<>();
}
