package com.example.usersapi.service;

import com.example.usersapi.dto.UserRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.entity.User;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASS_REGEX  = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

    @org.mockito.Mock UserRepository userRepository;
    @org.mockito.Mock PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, EMAIL_REGEX, PASS_REGEX, passwordEncoder);
    }

    // helper
    private UserRequest reqValido(List<UserRequest.PhoneRequest> phones) {
        // uso mayúsculas y espacios para comprobar que el servicio normaliza a lower/trim
        return new UserRequest("Gonzalo", "  G@MAIL.com  ", "Abcd1234", phones);
    }

    @Test
    void register_emailNull_lanzaValidationException() {
        var req = new UserRequest("Gonzalo", null, "Abcd1234", List.of());
        assertThrows(jakarta.validation.ValidationException.class, () -> service.register(req));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_passwordNull_lanzaValidationException() {
        var req = new UserRequest("Gonzalo", "g@mail.com", null, List.of());
        assertThrows(jakarta.validation.ValidationException.class, () -> service.register(req));
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_emailDuplicado_lanzaDuplicateEmailException() {
        when(userRepository.existsByEmail("g@mail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.register(reqValido(List.of())));

        verify(userRepository).existsByEmail("g@mail.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_ok_sinPhones_guardaHash() {
        when(userRepository.existsByEmail("g@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("Abcd1234")).thenReturn("{bcrypt}HASH");
        // devolvemos el mismo objeto que se guarda para poder inspeccionarlo
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(returnsFirstArg());

        UserResponse res = service.register(reqValido(null));

        // respuesta
        assertThat(res.getEmail()).isEqualTo("g@mail.com");
        assertThat(res.getPhones()).isEmpty();
        assertThat(res.getId()).isNotBlank();
        assertThat(res.getToken()).isNotBlank();

        // verificación de cifrado
        verify(passwordEncoder).encode("Abcd1234");
        assertThat(captor.getValue().getPassword()).isEqualTo("{bcrypt}HASH");
    }

    @Test
    void register_ok_conPhones_mapeaTelefonos() {
        when(userRepository.existsByEmail("g@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}HASH");
        when(userRepository.save(any(User.class))).thenAnswer(returnsFirstArg());

        var phones = List.of(new UserRequest.PhoneRequest("1234567", "1", "56"));
        UserResponse res = service.register(reqValido(phones));

        assertThat(res.getPhones()).hasSize(1);
        assertThat(res.getPhones().get(0).getNumber()).isEqualTo("1234567");
        assertThat(res.getPhones().get(0).getCitycode()).isEqualTo("1");
        assertThat(res.getPhones().get(0).getContrycode()).isEqualTo("56");

        verify(passwordEncoder).encode("Abcd1234");
        verify(userRepository).save(any(User.class));
    }
}