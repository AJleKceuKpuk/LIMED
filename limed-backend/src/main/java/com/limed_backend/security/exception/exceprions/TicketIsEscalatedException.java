package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class TicketIsEscalatedException extends AppException {
    public TicketIsEscalatedException() {
        super(ErrorCode.ERROR_TICKET_IS_ESCALATED);
    }
}
