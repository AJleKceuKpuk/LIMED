package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Chat.ChatResponse;
import com.limed_backend.security.entity.Chats;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ChatsMapper {

    @Named("toChatResponse")
    default ChatResponse toChatResponse(Chats chat) {
        List<String> usernames = chat.getChatUsers().stream()
                .map(chatUser -> chatUser.getUser().getUsername())
                .collect(Collectors.toList());

        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                usernames,
                chat.getStatus(),
                (long)chat.getMessages().size()
        );
    }
}