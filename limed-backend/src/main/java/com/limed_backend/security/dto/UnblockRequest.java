package com.limed_backend.security.dto;

import lombok.Data;

@Data
public class UnblockRequest {
    private String blockingType;
    private String username;
}
