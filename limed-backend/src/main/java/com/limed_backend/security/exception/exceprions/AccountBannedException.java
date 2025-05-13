package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class AccountBannedException extends AppException {
    public AccountBannedException() {
        super(ErrorCode.ERROR_BANNED);
    }
}