package com.limed_backend.security.dto;

import lombok.Data;

@Data
public class UserStatusRequest {
    private Long userId;
    private String status;

    public UserStatusRequest(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }
}