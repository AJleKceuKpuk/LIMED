package com.limed_backend.security.dto.Support;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class SupportMessageResponse implements Serializable {

    private Long id;
    private String content;
    private LocalDateTime sendTime;
    private LocalDateTime editedAt;
    private Long supportId;
    private Long senderId;

}
