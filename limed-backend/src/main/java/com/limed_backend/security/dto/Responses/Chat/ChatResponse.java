package com.limed_backend.security.dto.Responses.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String type;
    private List<String> username;
    private String status;
    private Long count;
}