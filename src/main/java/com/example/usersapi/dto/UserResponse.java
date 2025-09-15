package com.example.usersapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;

    private LocalDateTime created;
    private LocalDateTime modified;
    @JsonProperty("last_login")
    private LocalDateTime lastLogin;

    private String token;
    @JsonProperty("isactive")
    private boolean isActive;

    private List<PhoneResponse> phones;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneResponse {
        private String number;
        private String citycode;
        private String contrycode;
    }
}
