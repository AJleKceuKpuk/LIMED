package com.limed_backend.security.dto.Message;

import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private Long id;
    private Long chatId;
    private String type;
    private String content;
    private String metadata;
    private List<Long> usersId;
    private Long viewerId;
}
