package com.example.usersapi.controller;

import com.example.usersapi.repository.UserRepository;
import com.example.usersapi.security.TokenService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String email = req.email() == null ? null : req.email().trim().toLowerCase();
        var opt = userRepository.findByEmail(email);
        if (opt.isEmpty())
            return ResponseEntity.status(401).body(Map.of("mensaje","Credenciales inválidas"));

        var u = opt.get();
        if (!passwordEncoder.matches(req.password(), u.getPassword()))
            return ResponseEntity.status(401).body(Map.of("mensaje","Credenciales inválidas"));

        String jti = UUID.randomUUID().toString();
        u.setToken(jti);             // <-- jti vigente
        userRepository.save(u);      // persistir cambio

        var token = tokenService.createToken(u.getId(), List.of("ROLE_USER"), jti);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record TokenResponse(String token) {}
}
