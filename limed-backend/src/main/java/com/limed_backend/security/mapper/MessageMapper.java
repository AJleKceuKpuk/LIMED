package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Message.MessageResponse;
import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Named("toChatResponse")
    default MessageResponse toMessageResponse(Messages messages) {

        return new MessageResponse(
                messages.getId(),
                messages.getChat().getId(),
                messages.getType(),
                messages.getSendTime(),
                messages.getSender().getUsername(),
                messages.getSender().getId(),
                messages.getContent(),
                messages.getMetadata(),
                messages.getEditedAt()
        );
    }
}