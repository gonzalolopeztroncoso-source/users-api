package com.example.usersapi.controller;

import com.example.usersapi.dto.UserRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.exception.GlobalExceptionHandler;
import com.example.usersapi.exception.ValidationException;
import com.example.usersapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerMvcTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserService userService;
    @MockBean JwtDecoder jwtDecoder; // evita crear el real en el slice

    // ===== helpers =====
    private String bodyValido() throws Exception {
        var req = new UserRequest(
                "Gonzalo",
                "g@mail.com",
                "Abcd1234",
                List.of(new UserRequest.PhoneRequest("1234567", "1", "56"))
        );
        return om.writeValueAsString(req);
    }

    private String bodyInvalido() throws Exception {
        var req = new UserRequest(
                "G",           // nombre muy corto si tienes @Size, o mantenlo igual
                "mal-email",   // rompe @Email
                "abc",         // rompe tu regex de password
                List.of()
        );
        return om.writeValueAsString(req);
    }

    private UserResponse stub() {
        return new UserResponse(
                UUID.randomUUID().toString(),
                "Gonzalo",
                "g@mail.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "token",
                true,
                List.of(new UserResponse.PhoneResponse("1234567","1","56"))
        );
    }

    // ===== tests =====

    @Test
    void crearUsuario_201() throws Exception {
        when(userService.register(ArgumentMatchers.any())).thenReturn(stub());

        mvc.perform(post("/api/users")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void crearUsuario_400_porBeanValidation() throws Exception {
        // NO mockeamos userService: la validación de @Valid debe fallar antes
        mvc.perform(post("/api/users")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists()); // tu handler llena "mensaje"
    }

    @Test
    void crearUsuario_400_porValidationException_delServicio() throws Exception {
        when(userService.register(ArgumentMatchers.any()))
                .thenThrow(new ValidationException("Password inválida"));

        mvc.perform(post("/api/users")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Password inválida"));
    }

    @Test
    void crearUsuario_409_porDuplicateEmailException() throws Exception {
        when(userService.register(ArgumentMatchers.any()))
                .thenThrow(new DuplicateEmailException());

        mvc.perform(post("/api/users")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    void crearUsuario_500_porErrorInesperado() throws Exception {
        when(userService.register(ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/api/users")
                        .with(jwt().authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error inesperado"));
    }
}
