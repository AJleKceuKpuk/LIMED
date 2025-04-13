package com.limed_backend.component;

import lombok.Data;

@Data
public class SingupRequest {
    private String username;
    private String email;
    private String password;
    private String roles;
}
