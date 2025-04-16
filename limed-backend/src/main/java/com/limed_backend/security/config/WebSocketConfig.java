package com.limed_backend.security.config;

import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Включаем простой брокер для каналов сообщений (для рассылки обновлений клиентам)
        registry.enableSimpleBroker("/topic");
        // Все сообщения, отправляемые клиентом, должны начинаться с префикса /app
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем конечную точку, через которую клиенты будут подключаться к WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // На продакшене стоит ограничить домены
                .withSockJS();                 // Поддержка SockJS для несовместимых браузеров
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // Предположим, что в ходе установления соединения в header помещается userId как строка
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("userId");

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                userService.updateStatus(userId, "offline");
                System.out.println("User " + userId + " disconnect. Status Offline.");
            } catch (NumberFormatException e) {
                System.err.println("Error to  userId on int: " + userIdStr);
            }
        } else {
            System.out.println("Session no userId disconnct.");
        }
    }
}