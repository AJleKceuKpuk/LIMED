package com.limed_backend.security.dto.Requests;

import lombok.Data;

@Data
public class RenameChatRequest {
    private Long id;
    private String newName;
}
