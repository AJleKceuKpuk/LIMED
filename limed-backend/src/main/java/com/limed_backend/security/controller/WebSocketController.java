package com.limed_backend.security.controller;

import com.limed_backend.security.dto.UserStatus;
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


    @MessageMapping("/user/status")
    @SendTo("/topic/user/status")
    public UserStatus updateUserStatus(UserStatus message) {
        // Обновляем время активности пользователя
        connectionService.updateUserActivity(message.getUserId());

        // Обновляем статус в базе

        System.out.println("Update DB online");
        userService.updateOnlineStatus(message.getUserId(), message.isOnline());

        return message;
    }

//    @MessageMapping("/user/status")
//    @SendTo("/topic/user/status")
//    public UserStatus updateUserStatus(UserStatus message) {
//        System.out.println("Получено сообщение: " + message);
//        userService.updateOnlineStatus(message.getUserId(), message.isOnline());
//        return message;
//    }


}