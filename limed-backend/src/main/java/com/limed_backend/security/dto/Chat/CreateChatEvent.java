package com.limed_backend.security.dto.Chat;

import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.entity.enums.ChatEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class CreateChatEvent {
    // Геттеры и сеттеры
    private ChatEventType eventType;
    private Chats chat;
    private User user; // пользователь, инициировавший событие
    private Map<String, Object> payload; // дополнительные параметры

    public CreateChatEvent(ChatEventType eventType, Chats chat, User user) {
        this.eventType = eventType;
        this.chat = chat;
        this.user = user;
        this.payload = new HashMap<>();
    }

    public void putPayload(String key, Object value) {
        this.payload.put(key, value);
    }

}

