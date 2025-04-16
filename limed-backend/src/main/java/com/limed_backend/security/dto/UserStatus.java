package com.limed_backend.security.dto;

import lombok.Data;

@Data
public class UserStatus {
    private Long userId;
    private boolean online;
}