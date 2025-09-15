package com.example.usersapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "El formato del correo no es válido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    private List<PhoneRequest> phones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneRequest {
        private String number;
        private String citycode;
        private String contrycode;
    }
}
