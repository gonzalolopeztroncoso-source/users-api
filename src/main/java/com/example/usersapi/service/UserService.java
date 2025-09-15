package com.example.usersapi.service;

import com.example.usersapi.dto.UserRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.entity.Phone;
import com.example.usersapi.entity.User;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import java.util.regex.Pattern;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Pattern emailPattern;
    private final Pattern passwordPattern;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,
                       @Value("${app.regex.email}") String emailRegex,
                       @Value("${app.regex.password}") String passwordRegex,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailPattern = Pattern.compile(emailRegex);
        this.passwordPattern = Pattern.compile(passwordRegex);
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(UserRequest req) {
        var email = req.getEmail() == null ? null : req.getEmail().trim().toLowerCase();
        //validaciones de formato
        if (email == null || !emailPattern.matcher(email).matches()) {
            throw new ValidationException("El correo no cumple el formato");
        }
        if (req.getPassword() == null || !passwordPattern.matcher(req.getPassword()).matches()) {
            throw new ValidationException("La clave no cumple el formato");
        }
        // Duplicado
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        var now = LocalDateTime.now();

        User u = User.builder()
                .id(UUID.randomUUID().toString())
                .name(req.getName())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .created(now)
                .modified(now)
                .lastLogin(now)
                .token(UUID.randomUUID().toString())
                .isActive(true)
                .build();

        if (req.getPhones() != null) {
            var phones = req.getPhones().stream().map(p -> Phone.builder()
                    .number(p.getNumber())
                    .citycode(p.getCitycode())
                    .contrycode(p.getContrycode())
                    .user(u)
                    .build()
            ).collect(Collectors.toList());
            u.setPhones(phones);
        }
        userRepository.save(u);

        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .created(u.getCreated())
                .modified(u.getModified())
                .lastLogin(u.getLastLogin())
                .token(u.getToken())
                .isActive(u.isActive())
                .phones(u.getPhones().stream().map(ph ->
                        UserResponse.PhoneResponse.builder()
                                .number(ph.getNumber())
                                .citycode(ph.getCitycode())
                                .contrycode(ph.getContrycode())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
