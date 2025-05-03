package com.limed_backend.security.service;

import com.limed_backend.security.dto.Responses.ContactsPendingResponse;
import com.limed_backend.security.dto.Responses.ContactsResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ContactsMapper;
import com.limed_backend.security.repository.ContactsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactsService {

    private final UserService userService;
    private final ContactsRepository contactsRepository;
    private final ContactsMapper contactsMapper;
    private final CacheManager cacheManager;

    public void evictContactsCaches(User user) {
        Cache contactsCache = cacheManager.getCache("ContactsCache");
        if (contactsCache != null) {

            String KeyPending = user.getUsername() + "-Pending";
            String KeyInvitation = user.getUsername() + "-Invitation";
            String keyIgnore = user.getUsername() + "-Ignore";
            String KeyGeneral = String.valueOf(user.getId());

            contactsCache.evict(KeyPending);
            contactsCache.evict(KeyInvitation);
            contactsCache.evict(keyIgnore);
            contactsCache.evict(KeyGeneral);
        }
    }

    //=====================================================//
    // Список всех друзей пользователя
    @Cacheable(value = "ContactsCache", key = "#userId")
    public List<ContactsResponse> findAllContacts(User user) {
        List<Contacts> contacts = contactsRepository.findAcceptedByUser(user.getId());
        return contacts.stream()
                .map(contact -> contactsMapper.toContactsResponse(contact, user.getId()))
                .collect(Collectors.toList());
    }

    // Исходящие заявки
    @Cacheable(value = "ContactsCache", key = "#authentication.name + '-Pending'")
    public List<ContactsPendingResponse> findPendingContacts(Authentication authentication) {
        User sender = userService.findUserByUsername(authentication.getName());
        List<Contacts> outgoingRequests = contactsRepository.findBySender_IdAndStatus(sender.getId(), "Pending");
        return outgoingRequests.stream()
                .map(contacts -> {
                    ContactsResponse response = contactsMapper.toContactsResponse(contacts, sender.getId());
                    ContactsPendingResponse pendingResponse = new ContactsPendingResponse();
                    pendingResponse.setId(response.getId());
                    pendingResponse.setUsername(response.getUsername());
                    return pendingResponse;
                })
                .collect(Collectors.toList());
    }

    // Входящие заявки
    @Cacheable(value = "ContactsCache", key = "#authentication.name + '-Invitation'")
    public List<ContactsPendingResponse> findInvitationContacts(Authentication authentication) {
        User receiver = userService.findUserByUsername(authentication.getName());
        List<Contacts> incomingRequests = contactsRepository.findByReceiver_IdAndStatus(receiver.getId(), "Pending");
        return incomingRequests.stream()
                .map(contacts -> {
                    ContactsResponse response = contactsMapper.toContactsResponse(contacts, receiver.getId());
                    ContactsPendingResponse pendingResponse = new ContactsPendingResponse();
                    pendingResponse.setId(response.getId());
                    pendingResponse.setUsername(response.getUsername());
                    return pendingResponse;
                })
                .collect(Collectors.toList());
    }

    //Черный список
    @Cacheable(value = "ContactsCache", key = "#authentication.name + '-Ignore'")
    public List<ContactsPendingResponse> findIgnoreContacts(Authentication authentication) {
        User sender = userService.findUserByUsername(authentication.getName());
        List<Contacts> outgoingRequests = contactsRepository.findBySender_IdAndStatus(sender.getId(), "Ignore");
        return outgoingRequests.stream()
                .map(contacts -> {
                    ContactsResponse response = contactsMapper.toContactsResponse(contacts, sender.getId());
                    ContactsPendingResponse pendingResponse = new ContactsPendingResponse();
                    pendingResponse.setId(response.getId());
                    pendingResponse.setUsername(response.getUsername());
                    return pendingResponse;
                })
                .collect(Collectors.toList());
    }

    // Поиск связи между пользователями со статусом
    public Optional<Contacts> findContactsAccepted(Long senderId, Long receiverId) {
        return contactsRepository.findContactBetween(senderId, receiverId, "Accepted");
    }

    //поиск связи в одностороннем порядке
    public Optional<Contacts> findDirectStatus(Long senderId, Long receiverId, String status){
        return contactsRepository.findDirectContact(senderId, receiverId, status);
    }

    //проверка, что дружба реально существует
    public boolean isAcceptedContacts(Long senderId, Long receiverId){
        return contactsRepository.findContactBetween(senderId, receiverId, "Accepted").isPresent();
    }

    //=====================================================//
    // Метод добавления дружбы
    public String addContacts(Authentication authentication, Long receiverId) {
        User sender = userService.findUserByUsername(authentication.getName());
        User receiver = userService.findUserById(receiverId);

        if (sender.getId().equals(receiverId)) {
            return "Вы не можете себе отправить дружбу";
        }
        Optional<Contacts> alreadyIgnored = findDirectStatus(receiver.getId(),sender.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь заблокировал Вас";
        }

        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }

        Optional<Contacts> outgoingInvitation = findDirectStatus(sender.getId(), receiver.getId(), "Pending");
        if (outgoingInvitation.isPresent()) {
            return "Предложение подружиться уже отправлено";
        }

        Optional<Contacts> contactInvitation = findDirectStatus(receiver.getId(), sender.getId(), "Pending");
        if (contactInvitation.isPresent()) {
            Contacts contact = contactInvitation.get();
            contact.setStatus("Accepted");
            contactsRepository.save(contact);

            evictContactsCaches(receiver);
            evictContactsCaches(sender);

            return "Предложение подружиться принято";
        }

        Contacts newInvitation = Contacts.builder()
                .sender(sender)
                .receiver(receiver)
                .status("Pending")
                .build();
        contactsRepository.save(newInvitation);

        evictContactsCaches(receiver);
        evictContactsCaches(sender);

        return "Предложение подружиться отправлено";
    }

    //Добавить в черный список
    public String addIgnore(Authentication authentication, Long receiverId) {
        User sender = userService.findUserByUsername(authentication.getName());

        if (sender.getId().equals(receiverId)) {
            return "Вы не можете игнорировать себя";
        }

        User receiver = userService.findUserById(receiverId);
        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }

        Optional<Contacts> alreadyIgnored = findDirectStatus(sender.getId(), receiver.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь уже заблокирован";
        }

        Optional<Contacts> outgoingInvitation = findDirectStatus(sender.getId(), receiver.getId(), "Pending");
        Optional<Contacts> incomingInvitation = findDirectStatus(receiver.getId(), sender.getId(), "Pending");

        if (incomingInvitation.isPresent()) {
            contactsRepository.delete(incomingInvitation.get());
        } else outgoingInvitation.ifPresent(contactsRepository::delete);

        Contacts newIgnore = Contacts.builder()
                .sender(sender)
                .receiver(receiver)
                .status("Ignore")
                .build();
        contactsRepository.save(newIgnore);
        if (incomingInvitation.isPresent() || outgoingInvitation.isPresent()) {
            return "Предложение подружиться отклонено, и пользователь заблокирован";
        }
        evictContactsCaches(receiver);
        evictContactsCaches(sender);
        return "Пользователь заблокирован";
    }

    // Метод принятия дружбы
    public String acceptContacts(Authentication authentication, Long senderId) {
        User receiver = userService.findUserByUsername(authentication.getName());
        User sender = userService.findUserById(senderId);

        if (isAcceptedContacts(senderId, receiver.getId())) {
            return "Пользователь уже является вашим другом";
        }

        Optional<Contacts> contactsInvitationOpt = findDirectStatus(senderId, receiver.getId(), "Pending");
        if (contactsInvitationOpt.isEmpty()) {
            throw new ResourceNotFoundException("У вас нет предложений для дружбы от пользователя с id " + senderId);
        }

        Contacts contactsInvitation = contactsInvitationOpt.get();
        contactsInvitation.setStatus("Accepted");
        contactsRepository.save(contactsInvitation);

        evictContactsCaches(receiver);
        evictContactsCaches(sender);
        return "Предложение принято";
    }

    // Метод отказа от дружбы (отклонения входящего предложения)
    public String cancelContacts(Authentication authentication, Long senderId) {
        User receiver = userService.findUserByUsername(authentication.getName());
        User sender = userService.findUserById(senderId);
        Optional<Contacts> contactsInvitation = findDirectStatus(senderId, receiver.getId(), "Pending");
        if (contactsInvitation.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет предложений для дружбы от пользователя с id " + senderId);
        }
        contactsRepository.delete(contactsInvitation.get());
        evictContactsCaches(receiver);
        evictContactsCaches(sender);
        return "Предложение отклонено";
    }

    // Метод удаления друга
    public String deleteContacts(Authentication authentication, Long senderId) {
        User receiver = userService.findUserByUsername(authentication.getName());
        User sender = userService.findUserById(senderId);
        if (receiver.getId().equals(senderId)) {
            return "Вы являетесь этим пользователем, удалить невозможно";
        }
        Optional<Contacts> acceptedContacts = findContactsAccepted(senderId, receiver.getId());
        if (acceptedContacts.isEmpty()) {
            return "Контакт не найден";
        }
        contactsRepository.delete(acceptedContacts.get());
        evictContactsCaches(receiver);
        evictContactsCaches(sender);
        return "Пользователь удален из списка друзей";
    }

    //Удалить из черного списка
    public String deleteIgnore(Authentication authentication, Long receiverId) {
        User sender = userService.findUserByUsername(authentication.getName());
        User receiver = userService.findUserById(receiverId);

        Optional<Contacts> ignoreUser = findDirectStatus(sender.getId(), receiverId, "Ignore");
        if (ignoreUser.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет игнорируемых пользователей");
        }
        contactsRepository.delete(ignoreUser.get());
        evictContactsCaches(receiver);
        evictContactsCaches(sender);
        return "Пользователь " + receiver.getUsername() + " разблокирован";
    }
}
