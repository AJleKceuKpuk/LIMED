package com.limed_backend.security.websocket;

import com.limed_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;
    private final ConnectionService connectionService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // Пытаемся извлечь сначала из атрибутов
        headerAccessor.getSessionAttributes();
        Object userIdObj = Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        if (userIdObj == null) {
            headerAccessor.getUser();
            userIdObj = Objects.requireNonNull(headerAccessor.getUser()).getName();
        }

        if (userIdObj != null) {
            try {
                Long userId = Long.parseLong(userIdObj.toString());
                userService.updateUserStatus(userId, "offline");
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
