package com.example.usersapi.controller;

import com.example.usersapi.dto.UserRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Crear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ErrorMsg.class))),
            @ApiResponse(responseCode = "409", description = "Email ya registrado",
                    content = @Content(schema = @Schema(implementation = ErrorMsg.class)))
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        var res = userService.register(request);
        return ResponseEntity.status(201).body(res);
    }

    public record ErrorMsg(String mensaje) {}
}
