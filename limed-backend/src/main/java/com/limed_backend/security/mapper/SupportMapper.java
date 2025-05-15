package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Support.SupportMessageResponse;
import com.limed_backend.security.dto.Support.SupportResponse;
import com.limed_backend.security.entity.Support;
import com.limed_backend.security.entity.SupportMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

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
                support.getUser().getUsername(),
                support.isReadByUser(),
                support.isReadByAdmin()
        );
    }

    @Named("toSupportMessageResponse")
    default SupportMessageResponse toSupportMessageResponse(SupportMessage message){

        return new SupportMessageResponse(
                message.getId(),
                message.getContent(),
                message.getSendTime(),
                message.getEditedAt(),
                message.getSupport().getId(),
                message.getSender().getId()
        );
    }
}