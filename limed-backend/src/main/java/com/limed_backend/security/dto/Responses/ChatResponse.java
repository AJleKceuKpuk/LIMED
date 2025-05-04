package com.limed_backend.security.dto.Responses;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
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