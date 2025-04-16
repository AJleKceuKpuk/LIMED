package com.limed_backend.security.config;


import com.limed_backend.security.service.CustomHandshakeHandler;
import com.limed_backend.security.service.HttpHandshakeInterceptor;
import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private  UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Включаем простой брокер для каналов сообщений (для рассылки обновлений клиентам)
        registry.enableSimpleBroker("/ws/online/users");
        // Все сообщения, отправляемые клиентом, должны начинаться с префикса /app
        registry.setApplicationDestinationPrefixes("/ws/online");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/online")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpHandshakeInterceptor())
                .setHandshakeHandler(new CustomHandshakeHandler())  // добавляем кастомный HandshakeHandler
                .withSockJS();
    }


}