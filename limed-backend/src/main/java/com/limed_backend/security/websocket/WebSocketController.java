package com.limed_backend.security.websocket;

import com.limed_backend.security.dto.Requests.UserStatusRequest;
import com.limed_backend.security.service.ConnectionService;
import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/update")
    public void updateUserStatus(UserStatusRequest message) {
        // Обновляем время активности и статус в базе
        connectionService.updateLastUserActivity(message.getUserId());
        userService.updateOnlineStatus(message.getUserId(), message.getStatus());

        // Отправляем сообщение только конкретному пользователю
        String destination = "/ws/online/users/" + message.getUserId();
        simpMessagingTemplate.convertAndSend(destination, message);
    }

}