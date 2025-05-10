package com.limed_backend.security.dto.Sanction;

import lombok.Data;


@Data
public class CreateSanctionRequest {
    private String username;
    private String sanctionType;
    private String duration;
    private String reason;
}
