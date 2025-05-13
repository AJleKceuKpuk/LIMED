package com.limed_backend.security.dto.Support;

import lombok.Data;

@Data
public class SupportCreateRequest {
    private String heading;
    private String type;
    private String message;
    private String metadata;
}
