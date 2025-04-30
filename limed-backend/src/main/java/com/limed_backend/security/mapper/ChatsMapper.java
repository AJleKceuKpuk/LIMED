package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.entity.Chats;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ChatsMapper {

    @Named("toChatResponse")
    default ChatResponse toChatResponse(Chats chat) {
        ChatResponse response = new ChatResponse();

        List<String> usernames = chat.getChatUsers().stream()
                .map(chatUser -> chatUser.getUser().getUsername())
                .collect(Collectors.toList());

        response.setId(chat.getId());
        response.setName(chat.getName());
        response.setType(chat.getType());
        response.setUsername(usernames);
        response.setStatus(chat.getStatus());
        response.setCount((long) chat.getMessages().size());
        return response;
    }
}