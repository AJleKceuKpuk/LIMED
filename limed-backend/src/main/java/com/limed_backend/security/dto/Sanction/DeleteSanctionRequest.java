package com.limed_backend.security.dto.Sanction;

import lombok.Data;

@Data
public class DeleteSanctionRequest {
    private String sanctionType;
    private String username;
}
