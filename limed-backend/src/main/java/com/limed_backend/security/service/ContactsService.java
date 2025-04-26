package com.limed_backend.security.service;

import com.limed_backend.security.dto.Responses.ContactsPendingResponse;
import com.limed_backend.security.dto.Responses.ContactsResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ContactsMapper;
import com.limed_backend.security.repository.ContactsRepository;
import lombok.RequiredArgsConstructor;
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


    public List<Contacts> findAcceptedContacts(Long senderId, Long receiverId) {
        List<Contacts> contacts = contactsRepository.
                findBySender_IdAndStatusOrReceiver_IdAndStatus(senderId,"Accepted",receiverId,"Accepted");
        if (!contacts.isEmpty()) {
            return contacts;
        }
        return null;
    }

    public boolean isAcceptedContacts(Long senderId, Long receiverId){
        return findContactsStatus(senderId, receiverId, "Accepted").isPresent() ||
                findContactsStatus(receiverId, senderId, "Accepted").isPresent();
    }

    // Поиск приглашения на дружбу со статусом "Pending" (проверяем направление, т.е. кто отправил запрос)
    private Optional<Contacts> findContactsStatus(Long senderId, Long receiverId, String status) {
        return contactsRepository.findBySender_IdAndReceiver_IdAndStatus(senderId, receiverId, status);
    }

    // Список друзей
    public List<ContactsResponse> getContacts(Authentication authentication) {
        User receiver = userService.getCurrentUser(authentication);

        List<Contacts> contacts = findAcceptedContacts(receiver.getId(), receiver.getId());

        return contacts.stream()
                .map(contact -> contactsMapper.toContactsResponse(contact, receiver.getId()))
                .collect(Collectors.toList());
    }

    // Исходящие заявки
    public List<ContactsPendingResponse> getPendingContacts(Authentication authentication) {
        User sender = userService.getCurrentUser(authentication);
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
    public List<ContactsPendingResponse> getInvitationContacts(Authentication authentication) {
        User receiver = userService.getCurrentUser(authentication);
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
    public List<ContactsPendingResponse> getIgnoreList(Authentication authentication) {
        User sender = userService.getCurrentUser(authentication);
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

    // Метод добавления дружбы
    public String addContacts(Authentication authentication, Long receiverId) {
        User sender = userService.getCurrentUser(authentication);
        User receiver = userService.findUserById(receiverId);

        if (sender.getId().equals(receiverId)) {
            return "Вы не можете себе отправить дружбу";
        }
        Optional<Contacts> alreadyIgnored = findContactsStatus(receiver.getId(),sender.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь заблокировал Вас";
        }

        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }

        Optional<Contacts> outgoingInvitation = findContactsStatus(sender.getId(), receiver.getId(), "Pending");
        if (outgoingInvitation.isPresent()) {
            return "Предложение подружиться уже отправлено";
        }

        Optional<Contacts> contactInvitation = findContactsStatus(receiver.getId(), sender.getId(), "Pending");
        if (contactInvitation.isPresent()) {
            contactInvitation.get().setStatus("Accepted");
            contactsRepository.save(contactInvitation.get());
            return "Предложение подружиться принято";
        }

        Contacts newInvitation = Contacts.builder()
                .sender(sender)
                .receiver(receiver)
                .status("Pending")
                .build();
        contactsRepository.save(newInvitation);
        return "Предложение подружиться отправлено";
    }

    //Добавить в черный список
    public String addIgnore(Authentication authentication, Long receiverId) {
        User sender = userService.getCurrentUser(authentication);

        if (sender.getId().equals(receiverId)) {
            return "Вы не можете игнорировать себя";
        }

        User receiver = userService.findUserById(receiverId);
        if (isAcceptedContacts(sender.getId(), receiver.getId())) {
            return "Пользователь является вашим другом";
        }

        Optional<Contacts> alreadyIgnored = findContactsStatus(sender.getId(), receiver.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь уже заблокирован";
        }

        Optional<Contacts> outgoingInvitation = findContactsStatus(sender.getId(), receiver.getId(), "Pending");
        Optional<Contacts> incomingInvitation = findContactsStatus(receiver.getId(), sender.getId(), "Pending");
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
        return "Пользователь заблокирован";
    }

    // Метод принятия дружбы
    public String acceptContacts(Authentication authentication, Long senderId) {
        User receiver = userService.getCurrentUser(authentication);
        userService.findUserById(senderId);

        if (isAcceptedContacts(senderId, receiver.getId())) {
            return "Пользователь уже является вашим другом";
        }

        Optional<Contacts> contactsInvitationOpt = findContactsStatus(senderId, receiver.getId(), "Pending");
        if (contactsInvitationOpt.isEmpty()) {
            throw new ResourceNotFoundException("У вас нет предложений для дружбы от пользователя с id " + senderId);
        }

        Contacts contactsInvitation = contactsInvitationOpt.get();
        contactsInvitation.setStatus("Accepted");
        contactsRepository.save(contactsInvitation);

        return "Предложение принято";
    }

    // Метод отказа от дружбы (отклонения входящего предложения)
    public String cancelContacts(Authentication authentication, Long senderId) {
        User receiver = userService.getCurrentUser(authentication);
        userService.findUserById(senderId);
        Optional<Contacts> contactsInvitation = findContactsStatus(senderId, receiver.getId(), "Pending");
        if (contactsInvitation.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет предложений для дружбы от пользователя с id " + senderId);
        }
        contactsRepository.delete(contactsInvitation.get());
        return "Предложение отклонено";
    }

    // Метод удаления друга
    public String deleteContacts(Authentication authentication, Long senderId) {
        User receiver = userService.getCurrentUser(authentication);
        userService.findUserById(senderId);
        if (receiver.getId().equals(senderId)) {
            return "Вы являетесь этим пользователем, удалить невозможно";
        }
        List<Contacts> acceptedContacts = findAcceptedContacts(senderId, receiver.getId());
        if (acceptedContacts == null || acceptedContacts.isEmpty()) {
            return "Контакт не найден";
        }
        contactsRepository.delete(acceptedContacts.get(0));
        return "Пользователь удален из списка друзей";
    }

    //Удалить из черного списка
    public String deleteIgnore(Authentication authentication, Long receiverId) {
        User sender = userService.getCurrentUser(authentication);
        User receiver = userService.findUserById(receiverId);

        Optional<Contacts> ignoreUser = findContactsStatus(sender.getId(), receiverId, "Ignore");
        if (ignoreUser.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет игнорируемых пользователей");
        }
        contactsRepository.delete(ignoreUser.get());
        return "Пользователь " + receiver.getUsername() + " разблокирован";
    }
}
