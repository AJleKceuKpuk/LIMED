package com.limed_backend.security.dto.Requests;

import lombok.Data;

import java.util.List;

@Data
public class CreateChatRequest {
    private String name;
    private String type;
    private List<Long> usersId;
}
