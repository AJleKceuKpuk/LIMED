package com.limed_backend.security.service;

import com.limed_backend.security.dto.Contact.ContactAddRequest;
import com.limed_backend.security.dto.Contact.NoFriendResponse;
import com.limed_backend.security.dto.Contact.FriendResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.mapper.ContactsMapper;
import com.limed_backend.security.repository.ContactsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactsService {

    private final ContactsRepository contactsRepository;
    private final ContactsMapper contactsMapper;
    private final UserCacheService userCache;
    private final ContactsCacheService contactsCache;


    /** Список всех друзей пользователя*/
    public List<FriendResponse> findAcceptContacts(User user) {
        System.out.println("find?");
        List<Contacts> contacts = contactsCache.findAllAccept(user.getId());
        System.out.println(contacts.toString());
        return contacts.stream()
                .map(contact -> contactsMapper.toFriendResponse(contact, user.getId()))
                .collect(Collectors.toList());
    }
    /** Исходящие заявки*/
    public List<NoFriendResponse> findPendingContacts(Authentication authentication) {
        User sender = userCache.findUserByUsername(authentication.getName());
        List<Contacts> pendingContacts = contactsCache.findAllPending(sender.getId());
        return pendingContacts.stream()
                .map(contact -> contactsMapper.toNoFriendResponse(contact, sender.getId()))
                .collect(Collectors.toList());
    }
    /** Входящие заявки*/
    public List<NoFriendResponse> findInviteContacts(Authentication authentication) {
        User receiver = userCache.findUserByUsername(authentication.getName());

        List<Contacts> inviteContacts = contactsCache.findAllInvite(receiver.getId());
        return inviteContacts.stream()
                .map(contact -> contactsMapper.toNoFriendResponse(contact, receiver.getId()))
                .collect(Collectors.toList());
    }
    /** Черный список*/
    public List<NoFriendResponse> findIgnoreContacts(Authentication authentication) {
        User sender = userCache.findUserByUsername(authentication.getName());
        List<Contacts> ignoreContacts = contactsCache.findAllIgnore(sender.getId());
        return ignoreContacts.stream()
                .map(contact -> contactsMapper.toNoFriendResponse(contact, sender.getId()))
                .collect(Collectors.toList());
    }


    /** Поиск связи между пользователями со статусом Accepted*/
    public Optional<Contacts> findContactsAccepted(Long senderId, Long receiverId) {
        return contactsRepository.findContactBetween(senderId, receiverId, "Accepted");
    }
    /** Поиск связи в одну сторону*/
    public Optional<Contacts> findDirectStatus(Long senderId, Long receiverId, String status){
        return contactsRepository.findDirectContact(senderId, receiverId, status);
    }
    /** Проверка, что дружба реально существует*/
    public boolean isAcceptedContacts(Long senderId, Long receiverId){
        return contactsRepository.findContactBetween(senderId, receiverId, "Accepted").isPresent();
    }


    /** Метод добавления в друзья*/
    @Transactional
    public String addContacts(Authentication authentication, ContactAddRequest request) {
        User sender = userCache.findUserByUsername(authentication.getName());
        User receiver;
        if (request.getId() != null){
            receiver = userCache.findUserById(request.getId());
        }else {
            receiver = userCache.findUserByUsername(request.getUsername());
        }

        if (sender.getId().equals(receiver.getId())) {
            return "Вы не можете себе отправить дружбу";
        }

        Optional<Contacts> alreadyIgnored = findDirectStatus(receiver.getId(),sender.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь заблокировал Вас";
        }
        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }
        Optional<Contacts> pending = findDirectStatus(sender.getId(), receiver.getId(), "Pending");
        if (pending.isPresent()) {
            return "Предложение подружиться уже отправлено";
        }
        Optional<Contacts> invite = findDirectStatus(receiver.getId(), sender.getId(), "Pending");
        if (invite.isPresent()) {
            Contacts contact = invite.get();
            contact.setStatus("Accepted");
            contactsRepository.save(contact);

            contactsCache.removeContactsFromCache(receiver, contact, "contacts-pending");
            contactsCache.removeContactsFromCache(sender, contact, "contacts-invite");
            contactsCache.addContactToCache(sender, contact, "contacts");
            contactsCache.addContactToCache(receiver, contact, "contacts");

            return "Предложение подружиться принято";
        }

        Contacts newContact = Contacts.builder()
                .sender(sender)
                .receiver(receiver)
                .status("Pending")
                .build();
        contactsRepository.save(newContact);

        contactsCache.addContactToCache(sender, newContact, "contacts-pending");
        contactsCache.addContactToCache(receiver, newContact, "contacts-pending");

        return "Предложение подружиться отправлено";
    }
    /** Добавить в черный список*/
    public String addIgnore(Authentication authentication, ContactAddRequest request) {
        User sender = userCache.findUserByUsername(authentication.getName());
        User receiver;
        if (request.getId() != null){
            receiver = userCache.findUserById(request.getId());
        }else {
            receiver = userCache.findUserByUsername(request.getUsername());
        }

        if (sender.getId().equals(request.getId())) {
            return "Вы не можете игнорировать себя";
        }
        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }

        Optional<Contacts> alreadyIgnored = findDirectStatus(sender.getId(), receiver.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь уже заблокирован";
        }

        Contacts outgoingInvitation = findDirectStatus(sender.getId(), receiver.getId(), "Pending").orElse(null);
        Contacts incomingInvitation = findDirectStatus(receiver.getId(), sender.getId(), "Pending").orElse(null);

        if (incomingInvitation != null) {
            contactsRepository.delete(incomingInvitation);
            contactsCache.removeContactsFromCache(receiver, incomingInvitation, "contacts-pending");
            contactsCache.removeContactsFromCache(sender, incomingInvitation, "contacts-invite");
        }
        if (outgoingInvitation != null){
            contactsRepository.delete(outgoingInvitation);
            contactsCache.removeContactsFromCache(sender, outgoingInvitation, "contacts-pending");
            contactsCache.removeContactsFromCache(receiver, outgoingInvitation, "contacts-invite");
        }

        Contacts newIgnore = Contacts.builder()
                .sender(sender)
                .receiver(receiver)
                .status("Ignore")
                .build();
        contactsRepository.save(newIgnore);

        contactsCache.addContactToCache(sender, newIgnore, "contacts-ignore");

        if (incomingInvitation != null || outgoingInvitation != null) {
            return "Предложение подружиться отклонено, и пользователь заблокирован";
        }

        return "Пользователь заблокирован";
    }
    /** Метод принятия дружбы*/
    public String acceptContacts(Authentication authentication, Long senderId) {
        User receiver = userCache.findUserByUsername(authentication.getName());
        User sender = userCache.findUserById(senderId);

        if (isAcceptedContacts(senderId, receiver.getId())) {
            return "Пользователь уже является вашим другом";
        }

        Contacts contactsInvite = findDirectStatus(senderId, receiver.getId(), "Pending")
                .orElseThrow(ResourceNotFoundException::new);

        contactsInvite.setStatus("Accepted");
        contactsRepository.save(contactsInvite);

        contactsCache.removeContactsFromCache(sender, contactsInvite, "contacts-pending");
        contactsCache.removeContactsFromCache(receiver, contactsInvite, "contacts-invite");
        contactsCache.addContactToCache(sender, contactsInvite, "contacts");
        contactsCache.addContactToCache(receiver, contactsInvite, "contacts");

        return "Предложение принято";
    }
    /** Метод отказа от дружбы (отклонения входящего предложения)*/
    public String cancelContacts(Authentication authentication, Long senderId) {
        User receiver = userCache.findUserByUsername(authentication.getName());
        User sender = userCache.findUserById(senderId);

        Contacts contactsInvite = findDirectStatus(senderId, receiver.getId(), "Pending").orElse(null);
        Contacts contactsPending = findDirectStatus(receiver.getId(), senderId, "Pending").orElse(null);

        if (contactsInvite != null){
            contactsRepository.delete(contactsInvite);
            contactsCache.removeContactsFromCache(sender, contactsInvite, "contacts-pending");
            contactsCache.removeContactsFromCache(receiver, contactsInvite, "contacts-invite");
        }
        else if (contactsPending != null){
            contactsRepository.delete(contactsPending);
            contactsCache.removeContactsFromCache(receiver, contactsPending, "contacts-pending");
            contactsCache.removeContactsFromCache(sender, contactsPending, "contacts-invite");
        }

        return "Предложение отклонено";
    }
    /** Метод удаления друга*/
    public String deleteContacts(Authentication authentication, Long senderId) {
        User receiver = userCache.findUserByUsername(authentication.getName());
        User sender = userCache.findUserById(senderId);
        if (receiver.getId().equals(senderId)) {
            return "Вы являетесь этим пользователем, удалить невозможно";
        }

        Contacts acceptedContacts = findContactsAccepted(senderId, receiver.getId())
                .orElseThrow(ResourceNotFoundException::new);

        contactsRepository.delete(acceptedContacts);

        contactsCache.removeContactsFromCache(sender, acceptedContacts, "contacts");
        contactsCache.removeContactsFromCache(receiver, acceptedContacts, "contacts");

        return "Пользователь удален из списка друзей";
    }
    /** Удалить из черного списка*/
    public String deleteIgnore(Authentication authentication, Long receiverId) {
        User sender = userCache.findUserByUsername(authentication.getName());
        User receiver = userCache.findUserById(receiverId);

        Contacts ignoreUser = findDirectStatus(sender.getId(), receiverId, "Ignore")
                .orElseThrow(ResourceNotFoundException::new);

        contactsRepository.delete(ignoreUser);

        contactsCache.removeContactsFromCache(sender, ignoreUser, "contacts-ignore");

        return "Пользователь " + receiver.getUsername() + " разблокирован";
    }

}
