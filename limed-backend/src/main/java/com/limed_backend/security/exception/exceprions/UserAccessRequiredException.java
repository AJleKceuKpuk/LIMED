package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class UserAccessRequiredException extends AppException {
    public UserAccessRequiredException() {
        super(ErrorCode.ERROR_FORBIDDEN);
    }
}
