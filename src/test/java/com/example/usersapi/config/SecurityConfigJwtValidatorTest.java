package com.example.usersapi.config;

import com.example.usersapi.entity.User;
import com.example.usersapi.repository.UserRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class SecurityConfigJwtValidatorTest {

    private static final String SECRET = "test-secret-32-chars-min-1234567890";

    private SecurityConfig newConfig() {
        SecurityProps props = new SecurityProps();
        props.setSecret(SECRET);
        props.setExpirationMinutes(60);
        return new SecurityConfig(props);
    }

    private String signToken(String sub, String jti, List<String> roles) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                .subject(sub)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(30, ChronoUnit.MINUTES)));
        if (jti != null) b.jwtID(jti);
        if (roles != null) b.claim("roles", roles);

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), b.build());
        jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    @Test
    void decode_ok_cuandoJtiCoincideConBD() throws Exception {
        // Arrange
        var repo = Mockito.mock(UserRepository.class);
        var cfg = newConfig();
        JwtDecoder dec = cfg.jwtDecoder(repo);

        String userId = "u1";
        String jti = "jti-123";
        String token = signToken(userId, jti, List.of("ROLE_USER"));

        User u = new User();
        u.setId(userId);
        u.setToken(jti); // coincide

        when(repo.findById(userId)).thenReturn(Optional.of(u));

        // Act
        Jwt jwt = dec.decode(token);

        // Assert
        assertThat(jwt.getSubject()).isEqualTo(userId);
        assertThat(jwt.getId()).isEqualTo(jti);
    }

    @Test
    void decode_falla_cuandoJtiNoCoincide() throws Exception {
        var repo = Mockito.mock(UserRepository.class);
        var cfg = newConfig();
        JwtDecoder dec = cfg.jwtDecoder(repo);

        String userId = "u1";
        String token = signToken(userId, "jti-ABC", List.of("ROLE_USER"));

        User u = new User();
        u.setId(userId);
        u.setToken("jti-OTHER"); // distinto

        when(repo.findById(userId)).thenReturn(Optional.of(u));

        assertThrows(JwtValidationException.class, () -> dec.decode(token));
    }

    @Test
    void decode_falla_cuandoNoHayJtiEnElToken() throws Exception {
        var repo = Mockito.mock(UserRepository.class);
        var cfg = newConfig();
        JwtDecoder dec = cfg.jwtDecoder(repo);

        String userId = "u1";
        String token = signToken(userId, null, List.of("ROLE_USER")); // sin jti

        User u = new User();
        u.setId(userId);
        u.setToken("jti-existente"); // BD tiene algo pero token viene sin jti

        when(repo.findById(userId)).thenReturn(Optional.of(u));

        assertThrows(JwtValidationException.class, () -> dec.decode(token));
    }
}
