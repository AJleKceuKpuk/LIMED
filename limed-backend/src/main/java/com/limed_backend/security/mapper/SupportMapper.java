package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Chat.ChatResponse;
import com.limed_backend.security.dto.Support.SupportResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.Support;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SupportMapper {

    @Named("toSupportResponse")
    default SupportResponse toSupportResponse(Support support) {

        return new SupportResponse(
                support.getId(),
                support.getHeading(),
                support.getType().getDisplayName(),
                support.getCreatedAt(),
                support.getStatus(),
                support.getUpdatedAt(),
                support.getUser().getUsername()
        );
    }
}