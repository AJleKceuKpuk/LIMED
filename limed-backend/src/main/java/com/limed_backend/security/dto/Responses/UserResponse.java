package com.limed_backend.security.dto.Responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.limed_backend.security.dto.Sanction.ActiveSanctionResponse;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime lastActivity;
    private LocalDate dateRegistration;
    private List<String> roles;
    private List<ActiveSanctionResponse> sanctions;
}
