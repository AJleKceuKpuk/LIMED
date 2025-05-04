package com.limed_backend.security.dto.Requests;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateRoleRequest {
    private Set<String> roles;   // Имена ролей, например, "ROLE_USER", "ROLE_ADMIN"

}