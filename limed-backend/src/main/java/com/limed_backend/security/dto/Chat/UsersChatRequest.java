package com.limed_backend.security.dto.Chat;

import lombok.Data;

import java.util.List;

@Data
public class UsersChatRequest {
    private Long id;
    private List<Long> usersId;
}
