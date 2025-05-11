package com.limed_backend.security.dto.Message;

import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private Long id;
    private Long chatId;
    private String type; //обязательно
    private String content; //обязательно
    private String metadata;
    private List<Long> usersId; //если нет чата то обязательно
    private Long viewerId;
}
