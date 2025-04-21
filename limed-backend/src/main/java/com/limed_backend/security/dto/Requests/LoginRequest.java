package com.limed_backend.security.dto.Requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
