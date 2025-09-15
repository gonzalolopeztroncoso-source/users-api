package com.example.usersapi.controller;

import com.example.usersapi.entity.User;
import com.example.usersapi.repository.UserRepository;
import com.example.usersapi.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean TokenService tokenService;
    @MockBean UserRepository userRepository;
    @MockBean org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    record Login(String email, String password) {}

    @Test
    void login_ok_retornatoken_y_llamaTokenService() throws Exception {
        var body = om.writeValueAsString(new Login("g@mail.com", "Abcd1234"));

        var user = new User();
        user.setId("u1");
        user.setPassword("{bcrypt}hash");

        // El controller usa findByEmail(...) y normaliza a minúsculas
        when(userRepository.findByEmail("g@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Abcd1234", user.getPassword())).thenReturn(true);
        // Persiste el jti en userRepository.save(u)
        when(userRepository.save(any(User.class))).thenAnswer(returnsFirstArg());
        // createToken(subject, roles, jti)
        when(tokenService.createToken(eq("u1"), eq(List.of("ROLE_USER")), anyString()))
                .thenReturn("fake.jwt.token");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake.jwt.token"));

        // Verifica que se llamó al token service con subject y roles (el jti es aleatorio)
        var subCaptor = ArgumentCaptor.forClass(String.class);
        var rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(tokenService).createToken(subCaptor.capture(), rolesCaptor.capture(), anyString());
        // assertEquals("u1", subCaptor.getValue());
        // assertEquals(List.of("ROLE_USER"), rolesCaptor.getValue());

        verify(userRepository).save(any(User.class)); // se guardó el jti
    }

    @Test
    void login_password_invalida_401() throws Exception {
        var body = om.writeValueAsString(new Login("g@mail.com", "malaClave"));

        var user = new User();
        user.setId("u1");
        user.setPassword("{bcrypt}hash");

        when(userRepository.findByEmail("g@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("malaClave", user.getPassword())).thenReturn(false);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

        verifyNoInteractions(tokenService);
        verify(userRepository, never()).save(any()); // no debe guardar jti si falla
    }

    @Test
    void login_usuario_no_existe_401() throws Exception {
        var body = om.writeValueAsString(new Login("noexiste@mail.com", "Abcd1234"));
        when(userRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));

        verifyNoInteractions(tokenService);
        verify(userRepository, never()).save(any());
    }
}
