package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class UnauthorizedAccessException extends AppException {
    public UnauthorizedAccessException() {
        super(ErrorCode.ERROR_FORBIDDEN);
    }
}
