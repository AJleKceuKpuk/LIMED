package com.limed_backend.security.dto.Sanction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InactiveSanctionResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String sanctionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private String sanctionedBy;
    private boolean revokedSanction;
    private String revokedBy;
}
