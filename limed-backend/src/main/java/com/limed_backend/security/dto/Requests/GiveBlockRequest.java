package com.limed_backend.security.dto.Requests;

import com.limed_backend.security.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GiveBlockRequest {
    private String username;
    private String blockingType;
    private String duration;
    private String reason;
}
