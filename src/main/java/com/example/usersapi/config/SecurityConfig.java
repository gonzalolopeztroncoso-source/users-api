package com.example.usersapi.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.example.usersapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProps.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProps props;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 console
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("USER") // requiere ROLE_USER
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())))
                .build();
    }

    // Claim "roles" -> GrantedAuthorities
    private JwtAuthenticationConverter jwtAuthConverter() {
        var conv = new JwtGrantedAuthoritiesConverter();
        conv.setAuthoritiesClaimName("roles");
        conv.setAuthorityPrefix(""); // dejamos las cadenas EXACTAS del claim (p.ej. "ROLE_USER")
        var jwtConv = new JwtAuthenticationConverter();
        jwtConv.setJwtGrantedAuthoritiesConverter(conv);
        return jwtConv;
    }

    // ÚNICO JwtDecoder: firma HS256 + validación de jti contra BD
    @Bean
    @ConditionalOnProperty(prefix = "security.jwt", name = "secret")
    JwtDecoder jwtDecoder(UserRepository repo) {
        SecretKey key = new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder dec = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> jtiValidator = jwt -> {
            String userId = jwt.getSubject();
            String jti    = jwt.getId(); // "jti" del token

            boolean ok = repo.findById(userId)
                    .map(u -> jti != null && jti.equals(u.getToken()))
                    .orElse(false);

            return ok
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "revoked_or_old_token", null));
        };

        dec.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), // exp, nbf, etc.
                jtiValidator                    // nuestra validación de jti
        ));
        return dec;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
