package com.limed_backend.security.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Регистрируем конечную точку подключения: клиентам достаточно обращаться к "/ws"
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpHandshakeInterceptor())   // твой интерцептор
                .setHandshakeHandler(new CustomHandshakeHandler())   // кастомный HandshakeHandler
                .withSockJS();
    }

    // Настраиваем брокер сообщений
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/ws/online", "/ws/chat", "/ws/unread");
        registry.setApplicationDestinationPrefixes("/ws");
    }
}