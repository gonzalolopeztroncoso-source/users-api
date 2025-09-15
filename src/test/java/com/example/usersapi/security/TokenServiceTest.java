package com.example.usersapi.security;

import com.example.usersapi.config.SecurityProps;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Test
    void createToken_firmaYClaims_ok() throws Exception {
        // props válidas (clave >= 32 bytes para HS256)
        var props = new SecurityProps();
        props.setSecret("test-secret-32-chars-min-1234567890");
        props.setExpirationMinutes(2);

        var service = new TokenService(props);

        var token = service.createToken("user-123", List.of("ROLE_USER"));

        // parseo y verificaciones
        var signed = SignedJWT.parse(token);

        // header
        assertEquals(JWSAlgorithm.HS256, signed.getHeader().getAlgorithm());

        // firma válida
        var verified = signed.verify(new MACVerifier(props.getSecret().getBytes(StandardCharsets.UTF_8)));
        assertTrue(verified);

        // claims
        JWTClaimsSet claims = signed.getJWTClaimsSet();
        assertEquals("user-123", claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.getStringListClaim("roles"));
        assertTrue(claims.getIssueTime().toInstant().isBefore(Instant.now().plusSeconds(5)));
        assertTrue(claims.getExpirationTime().toInstant().isAfter(Instant.now()));
    }

    @Test
    void createToken_claveCorta_lanzaRuntimeException() {
        var props = new SecurityProps();
        props.setSecret("short-key");
        props.setExpirationMinutes(2);

        var service = new TokenService(props);

        assertThrows(RuntimeException.class,
                () -> service.createToken("u", List.of("ROLE_USER")));
    }
}
