package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.BlockingResponse;
import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface BlockingMapper {

    @Mapping(target = "revokedBy", source = "revokedBy", qualifiedByName = "userToString")
    BlockingResponse toBlockingResponse(Blocking blocking);

    @Named("userToString")
    default String userToString(User user) {
        return user != null ? user.getUsername() : null;
    }
}