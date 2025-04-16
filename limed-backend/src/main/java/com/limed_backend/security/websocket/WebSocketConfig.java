package com.limed_backend.security.websocket;


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

        registry.enableSimpleBroker("/ws/online/users");
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