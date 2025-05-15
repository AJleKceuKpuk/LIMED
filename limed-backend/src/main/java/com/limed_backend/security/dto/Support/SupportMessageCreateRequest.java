package com.limed_backend.security.dto.Support;

import lombok.Data;

@Data
public class SupportMessageCreateRequest {
    private String content;
    private String metadata;
}
