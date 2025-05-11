package com.limed_backend.security.dto.Message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    private Long id;
    private Long chatId;
    private String type;
    private LocalDateTime sendTime;
    private String senderName;
    private Long senderId;
    private String content;
    private String metadata;
    private LocalDateTime editedAt;
}
