package com.limed_backend.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.limed_backend.security.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlockingResponse {
    private Long id;
    private String blockingType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private boolean revokedBlock;
    private String revokedBy;
}
