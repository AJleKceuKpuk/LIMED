package com.limed_backend.security.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {
    ERROR_AUTH(HttpStatus.UNAUTHORIZED, "ERROR_AUTH"),
    ERROR_FORBIDDEN(HttpStatus.FORBIDDEN, "ERROR_FORBIDDEN"),

    ERROR_BANNED(HttpStatus.FORBIDDEN, "ERROR_BANNED"),

    ERROR_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ERROR_RESOURCE_NOT_FOUND"),
    ERROR_USERNAME_EXISTS(HttpStatus.CONFLICT, "ERROR_USERNAME_EXISTS"),
    ERROR_EMAIL_EXISTS(HttpStatus.CONFLICT, "ERROR_EMAIL_EXISTS"),
    ERROR_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "ERROR_INVALID_REQUEST"),

    ERROR_TICKET_NOT_CLOSED(HttpStatus.BAD_REQUEST, "ERROR_TICKET_NOT_CLOSED"),
    ERROR_TICKET_IS_ESCALATED(HttpStatus.BAD_REQUEST, "ERROR_TICKET_IS_ESCALATED");

    private final HttpStatus httpStatus;
    private final String code;

    ErrorCode(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }
}