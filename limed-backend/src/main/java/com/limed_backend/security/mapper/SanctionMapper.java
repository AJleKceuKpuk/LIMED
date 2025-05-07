package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.SanctionResponse;
import com.limed_backend.security.entity.Sanction;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface SanctionMapper {

    @Mapping(target = "revokedBy", source = "revokedBy", qualifiedByName = "userToString")
    SanctionResponse toSanctionResponse(Sanction sanction);

    @Named("userToString")
    default String userToString(User user) {
        return user != null ? user.getUsername() : null;
    }
}