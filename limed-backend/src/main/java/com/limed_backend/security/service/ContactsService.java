package com.limed_backend.security.service;

import com.limed_backend.security.dto.Responses.ContactsPendingResponse;
import com.limed_backend.security.dto.Responses.ContactsResponse;
import com.limed_backend.security.entity.Contacts;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ContactsMapper;
import com.limed_backend.security.repository.ContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactsService {

    @Autowired
    private UserService userService;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private ContactsMapper contactsMapper;


    // Получаем текущего пользователя
    private User getCurrentUser(Authentication authentication) {
        return userService.findUserByUsername(authentication.getName());
    }

    // Получаем пользователя по переданному id
    private User getUserById(Long id) {
        return userService.findUserbyId(id);
    }


    // Поиск принятой дружбы между пользователями (проверяем обе стороны)
    private Optional<Contacts> findAcceptedContacts(Long senderId, Long receiverId) {
        return contactsRepository.findBySender_IdAndReceiver_IdAndStatus(senderId, receiverId, "Accepted")
                .or(() -> contactsRepository.findBySender_IdAndReceiver_IdAndStatus(receiverId, senderId, "Accepted"));
    }

    // Поиск приглашения на дружбу со статусом "Pending" (проверяем направление, т.е. кто отправил запрос)
    private Optional<Contacts> findPendingInvitation(Long senderId, Long receiverId) {
        return contactsRepository.findBySender_IdAndReceiver_IdAndStatus(senderId, receiverId, "Pending");
    }

    // Метод добавления дружбы
    public String addContacts(Authentication authentication, Long receiverId) {
        User sender = getCurrentUser(authentication);
        getUserById(receiverId);
        if (sender.getId().equals(receiverId)) {
            return "Вы не можете себе отправить дружбу";
        }
        User receiverUser = getUserById(receiverId);
        Optional<Contacts> alreadyIgnored = contactsRepository.findBySender_IdAndReceiver_IdAndStatus(receiverUser.getId(),sender.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь заблокировал Вас";
        }
        if (findAcceptedContacts(sender.getId(), receiverUser.getId()).isPresent()) {
            return "Пользователь является вашим другом";
        }
        Optional<Contacts> outgoingInvitation = findPendingInvitation(sender.getId(), receiverUser.getId());
        if (outgoingInvitation.isPresent()) {
            return "Предложение подружиться уже отправлено";
        }
        Optional<Contacts> contactInvitation = findPendingInvitation(receiverUser.getId(), sender.getId());
        if (contactInvitation.isPresent()) {
            contactInvitation.get().setStatus("Accepted");
            contactsRepository.save(contactInvitation.get());
            return "Предложение подружиться принято";
        }
        Contacts newInvitation = Contacts.builder()
                .sender(sender)
                .receiver(receiverUser)
                .status("Pending")
                .build();
        contactsRepository.save(newInvitation);
        return "Предложение подружиться отправлено";
    }

    // Метод принятия дружбы
    public String acceptContacts(Authentication authentication, Long senderId) {
        User receiver = getCurrentUser(authentication);
        getUserById(senderId);
        if (findAcceptedContacts(receiver.getId(), senderId).isPresent()) {
            return "Пользователь уже является вашим другом";
        }
        Optional<Contacts> contactsInvitationOpt = findPendingInvitation(senderId, receiver.getId());
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
        User receiver = getCurrentUser(authentication);
        getUserById(senderId);
        Optional<Contacts> contactsInvitation = findPendingInvitation(senderId, receiver.getId());
        if (contactsInvitation.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет предложений для дружбы от пользователя с id " + senderId);
        }
        contactsRepository.delete(contactsInvitation.get());
        return "Предложение отклонено";
    }

    // Метод удаления друга
    public String deleteContacts(Authentication authentication, Long senderId) {
        User receiver = getCurrentUser(authentication);
        getUserById(senderId);
        if (receiver.getId().equals(senderId)) {
            return "Вы являетесь этим пользователем, удалить невозможно";
        }
        Contacts acceptedContacts = findAcceptedContacts(senderId, receiver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не является вашим другом"));

        contactsRepository.delete(acceptedContacts);
        return "Пользователь удален из списка друзей";
    }

    // Метод возвращения списка друзей (с контактом со статусом "Accepted")
    public List<ContactsResponse> getContacts(Authentication authentication) {
        User receiver = getCurrentUser(authentication);
        List<Contacts> contacts = contactsRepository
                .findBySender_IdAndStatusOrReceiver_IdAndStatus(
                        receiver.getId(), "Accepted",
                        receiver.getId(), "Accepted");

        return contacts.stream()
                .map(contact -> contactsMapper.toContactsResponse(contact, receiver.getId()))
                .collect(Collectors.toList());
    }

    // Метод возвращает список пользователей, которым было отправлено предложение подружиться (исходящие заявки)
    public List<ContactsPendingResponse> getPendingContacts(Authentication authentication) {
        User sender = getCurrentUser(authentication);
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

    // Метод возвращает список пользователей, которые отправили вам предложение подружиться (входящие заявки)
    public List<ContactsPendingResponse> getInvitationContacts(Authentication authentication) {
        User receiver = getCurrentUser(authentication);
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

    //Добавить в черный список
    public String addIgnore(Authentication authentication, Long receiver) {
        User sender = getCurrentUser(authentication);
        if (sender.getId().equals(receiver)) {
            return "Вы не можете игнорировать себя";
        }
        User receiverUser = getUserById(receiver);
        if (findAcceptedContacts(sender.getId(), receiverUser.getId()).isPresent()) {
            return "Пользователь является вашим другом";
        }
        Optional<Contacts> alreadyIgnored = contactsRepository.
                findBySender_IdAndReceiver_IdAndStatus(sender.getId(), receiverUser.getId(), "Ignore");
        if(alreadyIgnored.isPresent()) {
            return "Пользователь уже заблокирован";
        }
        Optional<Contacts> outgoingInvitation = findPendingInvitation(sender.getId(), receiverUser.getId());
        Optional<Contacts> incomingInvitation = findPendingInvitation(receiverUser.getId(), sender.getId());
        if (incomingInvitation.isPresent()) {
            contactsRepository.delete(incomingInvitation.get());
        } else outgoingInvitation.ifPresent(contacts -> contactsRepository.delete(contacts));
        Contacts newIgnore = Contacts.builder()
                .sender(sender)
                .receiver(receiverUser)
                .status("Ignore")
                .build();
        contactsRepository.save(newIgnore);
        if (incomingInvitation.isPresent() || outgoingInvitation.isPresent()) {
            return "Предложение подружиться отклонено, и пользователь заблокирован";
        }
        return "Пользователь заблокирован";
    }

    //Удалить из черного списка
    public String deleteIgnore(Authentication authentication, Long receiverId) {
        User sender = getCurrentUser(authentication);
        User receiver = getUserById(receiverId);

        Optional<Contacts> ignoreUser = contactsRepository.findBySender_IdAndReceiver_IdAndStatus(sender.getId(), receiverId, "Ignore");
        if (ignoreUser.isEmpty()) {
            throw new ResourceNotFoundException("У Вас нет игнорируемых пользователей");
        }
        contactsRepository.delete(ignoreUser.get());
        return "Пользователь " + receiver.getUsername() + " разблокирован";
    }

    //Черный список
    public List<ContactsPendingResponse> getIgnoreList(Authentication authentication) {
        User sender = getCurrentUser(authentication);
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
}
