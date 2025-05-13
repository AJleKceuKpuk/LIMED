package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException() {
        super(ErrorCode.ERROR_USERNAME_EXISTS);
    }
}