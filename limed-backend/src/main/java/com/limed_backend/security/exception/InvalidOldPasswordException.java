package com.limed_backend.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOldPasswordException extends RuntimeException {
    public InvalidOldPasswordException() {
        super("Неверный старый пароль");
    }
}