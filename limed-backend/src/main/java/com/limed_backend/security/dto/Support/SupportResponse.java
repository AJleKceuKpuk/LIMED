package com.limed_backend.security.dto.Support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.limed_backend.security.entity.enums.SupportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class SupportResponse implements Serializable {

    private Long id;
    private String heading;
    private String type;
    private LocalDateTime createdAt;
    private SupportStatus status;
    private LocalDateTime updatedAt;
    private String username;

}
