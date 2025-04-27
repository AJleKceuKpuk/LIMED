package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Named("toChatResponse")
    default MessageResponse toMessageResponse(Messages messages) {

        MessageResponse response = new MessageResponse();
        response.setId(messages.getId());
        response.setChatId(messages.getChat().getId());
        response.setSendTime(messages.getSendTime());
        response.setSenderName(messages.getSender().getUsername());
        response.setSenderId(messages.getSender().getId());
        response.setContent(messages.getContent());
        List<String> usernames = messages.getViewedBy().stream()
                .map(User::getUsername)
                .toList();
        response.setViewedBy(usernames);

        response.setMetadata(messages.getMetadata());
        response.setEditedAt(messages.getEditedAt());
        return response;
    }
}