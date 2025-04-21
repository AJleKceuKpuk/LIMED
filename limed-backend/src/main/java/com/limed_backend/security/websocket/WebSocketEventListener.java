package com.limed_backend.security.websocket;

import com.limed_backend.security.service.ConnectionService;
import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Пытаемся извлечь сначала из атрибутов
        Object userIdObj = headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get("userId")
                : null;
        if (userIdObj == null && headerAccessor.getUser() != null) {
            userIdObj = headerAccessor.getUser().getName();
        }

        if (userIdObj != null) {
            try {
                Long userId = Long.parseLong(userIdObj.toString());
                userService.updateOnlineStatus(userId, "offline");
                userService.updateLastActivity(userId, LocalDateTime.now());
                connectionService.removeUser(userId);
            } catch (NumberFormatException e) {
                System.err.println("Error converting userId: " + userIdObj);
            }
        } else {
            System.out.println("Session no userId on disconnect.");
        }
    }
}
