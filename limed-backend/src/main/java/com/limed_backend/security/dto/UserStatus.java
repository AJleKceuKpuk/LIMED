package com.limed_backend.security.dto;


import lombok.Data;
import lombok.Getter;


@Getter
@Data
public class UserStatus {

    // Геттеры и сеттеры
    private Long userId;
    private String status;

    public UserStatus(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}