package com.limed_backend.security.dto.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private Long userId;
    private String status;
}