package com.example.usersapi.security;

import com.example.usersapi.config.SecurityProps;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final SecurityProps props;

    public String createToken(String subject, List<String> roles) {
        // Compatibilidad con código/test existente
        return createToken(subject, roles, UUID.randomUUID().toString());
    }

    public String createToken(String subject, List<String> roles, String jti) {
        var now = Instant.now();
        var claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .jwtID(jti)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(props.getExpirationMinutes(), ChronoUnit.MINUTES)))
                .claim("roles", roles)
                .build();

        var header = new JWSHeader(JWSAlgorithm.HS256);
        var jwt = new SignedJWT(header, claims);
        try {
            jwt.sign(new MACSigner(props.getSecret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new RuntimeException("Error firmando JWT", e);
        }
        return jwt.serialize();
    }
}
