package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException() {
        super(ErrorCode.ERROR_EMAIL_EXISTS);
    }
}