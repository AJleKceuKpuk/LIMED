package com.limed_backend.security.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {
    private LocalDateTime timestamp;
    private int status;
    private String error;      // код ошибки (например, ERROR_AUTH)
    private String message;    // ключ сообщения для локализации
    private String path;
}