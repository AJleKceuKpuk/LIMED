package com.limed_backend.security.dto.Responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.limed_backend.security.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    private Long id;
    private Long chatId;
    private LocalDateTime sendTime;
    private String senderName;
    private Long senderId;
    private String content;
    private List<String> viewedBy;
    private String metadata;
    private LocalDateTime editedAt;
}
