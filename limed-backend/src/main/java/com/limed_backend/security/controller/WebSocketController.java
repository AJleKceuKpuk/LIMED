package com.limed_backend.security.controller;

import com.limed_backend.security.dto.UserStatusRequest;
import com.limed_backend.security.service.UserConnectionService;
import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserConnectionService connectionService;


    @MessageMapping("/update")
    @SendTo("/ws/online/users")
    public UserStatusRequest updateUserStatus(UserStatusRequest message) {

        // Обновляем время активности пользователя
        connectionService.updateLastUserActivity(message.getUserId());
        // Обновляем статус в базе
        userService.updateOnlineStatus(message.getUserId(), message.getStatus());

        return message;
    }

}