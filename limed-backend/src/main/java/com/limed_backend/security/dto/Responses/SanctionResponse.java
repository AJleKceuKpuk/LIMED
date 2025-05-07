package com.limed_backend.security.dto.Responses;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SanctionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String sanctionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private boolean revokedSanction;
    private String revokedBy;
}
