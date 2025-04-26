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
        System.out.println(response.getId());
        response.setChatId(messages.getChat().getId());
        System.out.println(response.getChatId());
        response.setSendTime(messages.getSendTime());
        System.out.println(response.getSendTime());
        response.setSender(messages.getSender());
        System.out.println(response.getSender().getUsername());
        response.setContent(messages.getContent());
        System.out.println(response.getContent());
//        List<String> usernames = messages.getViewedBy().stream()
//                .map(User::getUsername)
//                .toList();
//        response.setViewedBy(usernames);

        response.setMetadata(messages.getMetadata());
        System.out.println(response.getMetadata());
        response.setEditedAt(messages.getEditedAt());
        System.out.println(response.getEditedAt());
        return response;
    }
}