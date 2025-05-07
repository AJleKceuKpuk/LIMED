package com.limed_backend.security.dto.Requests;

import lombok.Data;


@Data
public class GiveSanctionRequest {
    private String username;
    private String sanctionType;
    private String duration;
    private String reason;
}
