package com.limed_backend.security.dto.Requests;

import lombok.Data;

@Data
public class UnblockRequest {
    private String blockingType;
    private String username;
}
