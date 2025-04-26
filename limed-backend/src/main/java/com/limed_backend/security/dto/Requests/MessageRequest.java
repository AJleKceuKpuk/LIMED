package com.limed_backend.security.dto.Requests;

import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private Long id;
    private Long chatId;
    private String content;
    private String metadata;
    private List<Long> usersId;
}
