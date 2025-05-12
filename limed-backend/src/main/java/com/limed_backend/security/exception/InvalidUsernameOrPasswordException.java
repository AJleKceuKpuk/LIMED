package com.limed_backend.security.exception;

public class InvalidUsernameOrPasswordException extends AppException {
    public InvalidUsernameOrPasswordException() {
        super(ErrorCode.ERROR_AUTH);
    }
}