package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ChatsMapper {

    @Named("toChatResponse")
    default ChatResponse toChatResponse(Chats chat, Long currentUserId) {
        ChatResponse response = new ChatResponse();
        response.setId(chat.getId());
        response.setName(chat.getName());

        List<String> usernames = chat.getUsers().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(User::getUsername)
                .collect(Collectors.toList());

        response.setUsername(usernames);
        response.setStatus(chat.getStatus());
        return response;
    }
}