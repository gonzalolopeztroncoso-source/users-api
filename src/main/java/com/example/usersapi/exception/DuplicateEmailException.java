package com.example.usersapi.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends ApiException {
    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT,"El correo ya registrado");
    }
}
