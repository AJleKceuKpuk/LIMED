package com.limed_backend.security.dto.User;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateRoleRequest {
    private Long id;
    private Set<String> roles;
}