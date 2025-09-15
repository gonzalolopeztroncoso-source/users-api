package com.example.usersapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionTest {

    @Test
    void getStatus_devuelve_el_status_inyectado() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "mensaje");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("mensaje", ex.getMessage());
    }

    @Test
    void puede_lanzarse_y_capturarse_con_status() {
        try {
            throw new ApiException(HttpStatus.CONFLICT, "duplicado");
        } catch (ApiException e) {
            assertEquals(HttpStatus.CONFLICT, e.getStatus());
            assertEquals("duplicado", e.getMessage());
        }
    }
}
