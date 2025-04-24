package com.limed_backend.security.dto.Responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactsResponse {
    private Long id;
    private String username;
    private String status;
    private LocalDateTime lastActivity;
    private LocalDate dateRegistration;
}
