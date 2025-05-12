package com.limed_backend.security.exception;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }

    // Можно добавить конструктор с подробным описанием, если требуется
    public AppException(ErrorCode errorCode, String detail) {
        super(errorCode.getCode() + ": " + detail);
        this.errorCode = errorCode;
    }
}
