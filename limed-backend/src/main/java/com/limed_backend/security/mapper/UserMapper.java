package com.limed_backend.security.mapper;


import com.limed_backend.security.dto.User.UserResponse;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SanctionsMapper.class})
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    // Если санкции – всегда активные, можно указать конкретное преобразование:
    @Mapping(target = "sanctions", source = "sanctions", qualifiedByName = "toActiveSanctionResponse")
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

    // Метод для преобразования User в String
    default String map(User user) {
        return user != null ? user.getUsername() : null;
    }
}
