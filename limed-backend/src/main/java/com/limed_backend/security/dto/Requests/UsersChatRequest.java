package com.limed_backend.security.dto.Requests;

import lombok.Data;

import java.util.List;

@Data
public class UsersChatRequest {
    private Long id;
    private List<Long> usersId;
}
