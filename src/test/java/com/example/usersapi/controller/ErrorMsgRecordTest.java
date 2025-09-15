package com.example.usersapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMsgRecordTest {

    @Test
    void constructor_getter_y_json() throws Exception {
        var err = new UserController.ErrorMsg("falló");

        assertThat(err.mensaje()).isEqualTo("falló");

        var json = new ObjectMapper().writeValueAsString(err);
        assertThat(json).isEqualTo("{\"mensaje\":\"falló\"}");
    }
}
