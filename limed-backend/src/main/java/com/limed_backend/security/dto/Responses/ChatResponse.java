package com.limed_backend.security.dto.Responses;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private Long id;
    private String name;
    private String type;
    private List<String> username;
    private String status;
}