package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.FriendResponse;
import com.limed_backend.security.entity.Friends;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FriendsMapper {

    @Named("toFriendResponse")
    default FriendResponse toFriendResponse(Friends friendship, Long currentUserId) {
        FriendResponse response = new FriendResponse();
        // Определяем, кто является "другом": если текущий пользователь находится в поле user,
        // то в качестве друга берем значение поля friend, иначе наоборот.
        User friendUser;
        if (friendship.getUser().getId().equals(currentUserId)) {
            friendUser = friendship.getFriend();
        } else {
            friendUser = friendship.getUser();
        }
        response.setId(friendUser.getId());
        response.setUsername(friendUser.getUsername());
        response.setStatus(friendUser.getStatus()); // статус пользователя (online, offline, away)
        response.setLastActivity(friendUser.getLastActivity());
        response.setDateRegistration(friendUser.getDateRegistration());
        return response;
    }
}
