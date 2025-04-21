package com.limed_backend.security.mapper;


import com.limed_backend.security.dto.Responses.UserResponse;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {BlockingMapper.class})
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    @Mapping(target = "blocking", source = "blockings")
    UserResponse toUserResponse(User user);

    @Named("mapRoles")
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}