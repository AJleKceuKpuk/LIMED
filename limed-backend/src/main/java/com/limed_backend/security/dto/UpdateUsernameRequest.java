package com.limed_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UpdateUsernameRequest {
    private String newUsername;
}
