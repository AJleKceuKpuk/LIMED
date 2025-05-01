package com.limed_backend.security.websocket;

import org.springframework.context.annotation.Configuration;
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
        /*
         * Мы включаем простой брокер для подписок с префиксами:
         * - /ws/online – для онлайн статусов (подписка: /ws/online/{username})
         * - /ws/chat   – для сообщений чата (подписка: /ws/chat/{chatId})
         * - /ws/unread – для уведомлений о непрочитанных сообщениях (подписка: /ws/unread/{userid})
         */
        registry.enableSimpleBroker("/ws/online", "/ws/chat", "/ws/unread");

        /*
         * Задаём префикс для входящих (на сервер отправляемых) сообщений.
         * В данном случае клиент будет отправлять сообщения, например, по адресам:
         * - /ws/online/update  (обновление статуса)
         * - /ws/chat/send      (отправка сообщения в чат)
         */
        registry.setApplicationDestinationPrefixes("/ws");
    }
}