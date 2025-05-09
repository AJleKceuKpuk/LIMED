package com.limed_backend.security.dto.Chat;

import lombok.Data;

@Data
public class RenameChatRequest {
    private Long id;
    private String newName;
}
