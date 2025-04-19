package com.limed_backend.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.limed_backend.security.entity.Blocking;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime lastActivity;
    private LocalDate dateRegistration;

    private List<String> roles;
    private List<BlockingResponse> blocking;
}
