package com.limed_backend.security.dto.Requests;

import lombok.Data;

@Data
public class UnsanctionedRequest {
    private String sanctionType;
    private String username;
}
