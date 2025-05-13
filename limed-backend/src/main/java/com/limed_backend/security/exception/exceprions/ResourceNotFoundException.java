package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException() {
        super(ErrorCode.ERROR_RESOURCE_NOT_FOUND);
    }
}
