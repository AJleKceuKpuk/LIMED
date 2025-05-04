package com.limed_backend.security.dto.Responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactsPendingResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
}
