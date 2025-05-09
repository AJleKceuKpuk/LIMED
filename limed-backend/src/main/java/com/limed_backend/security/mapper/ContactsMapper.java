package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Contact.FriendResponse;
import com.limed_backend.security.dto.Contact.NoFriendResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ContactsMapper {

    @Named("toFriendResponse")
    default FriendResponse toFriendResponse(Contacts contacts, Long currentUserId) {
        // Определяем друга – если sender равен текущему пользователю, берем receiver, иначе sender.
        User friend = contacts.getSender().getId().equals(currentUserId)
                ? contacts.getReceiver()
                : contacts.getSender();
        return new FriendResponse(
                friend.getId(),
                friend.getUsername(),
                friend.getStatus(),
                friend.getLastActivity(),
                friend.getDateRegistration()
        );
    }

    @Named("toNoFriendResponse")
    default NoFriendResponse toNoFriendResponse(Contacts contacts, Long currentUserId) {

        User friend = contacts.getSender().getId().equals(currentUserId)
                ? contacts.getReceiver()
                : contacts.getSender();
        NoFriendResponse response = new NoFriendResponse();
        response.setId(friend.getId());
        response.setUsername(friend.getUsername());
        return new NoFriendResponse(
                friend.getId(),
                friend.getUsername()
        );
    }
}
