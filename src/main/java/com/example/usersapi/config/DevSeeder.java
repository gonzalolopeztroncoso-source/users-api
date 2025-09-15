package com.example.usersapi.config;

import com.example.usersapi.entity.User;
import com.example.usersapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
//@Profile("dev")
@RequiredArgsConstructor
public class DevSeeder {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            String email = "juan@rodriguez.org";
            repo.findByEmailIgnoreCase(email)
                    .orElseGet(() -> {
                        var now = LocalDateTime.now();
                        return repo.save(User.builder()
                                .id("seed-1")
                                .name("Juan Rodriguez")
                                .email(email)
                                .password(encoder.encode("Abcdef12"))
                                .created(now).modified(now).lastLogin(now)
                                .token(UUID.randomUUID().toString())
                                .isActive(true)
                                .build());
                    });
        };
    }
}
