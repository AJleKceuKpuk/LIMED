package com.limed_backend.security.exception.exceprions;

import com.limed_backend.security.exception.AppException;
import com.limed_backend.security.exception.ErrorCode;

public class TicketNotClosedException extends AppException {
    public TicketNotClosedException() {
        super(ErrorCode.ERROR_TICKET_NOT_CLOSED);
    }
}
