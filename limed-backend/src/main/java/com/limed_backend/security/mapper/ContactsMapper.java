package com.limed_backend.security.mapper;

import com.limed_backend.security.dto.Responses.ContactsResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ContactsMapper {

    @Named("toFriendResponse")
    default ContactsResponse toContactsResponse(Contacts contacts, Long currentUserId) {
        ContactsResponse response = new ContactsResponse();
        User receiverUser;
        if (contacts.getSender().getId().equals(currentUserId)) {
            receiverUser = contacts.getReceiver();
        } else {
            receiverUser = contacts.getSender();
        }
        response.setId(receiverUser.getId());
        response.setUsername(receiverUser.getUsername());
        response.setStatus(receiverUser.getStatus());
        response.setLastActivity(receiverUser.getLastActivity());
        response.setDateRegistration(receiverUser.getDateRegistration());
        return response;
    }
}
