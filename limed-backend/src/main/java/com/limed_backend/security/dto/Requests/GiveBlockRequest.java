package com.limed_backend.security.dto.Requests;

import lombok.Data;


@Data
public class GiveBlockRequest {
    private String username;
    private String blockingType;
    private String duration;
    private String reason;
}
