package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class AdminAccessRequiredException extends AppException {
    public AdminAccessRequiredException() {
        super(ErrorCode.ERROR_FORBIDDEN);
    }
}
