package com.limed_backend.security.dto;

import com.limed_backend.security.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GiveMutedRequest {
    private String blockingType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private User user;
}
