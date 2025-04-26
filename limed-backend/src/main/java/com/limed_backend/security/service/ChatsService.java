package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.RenameChatRequest;
import com.limed_backend.security.dto.Requests.UsersChatRequest;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ChatsMapper;
import com.limed_backend.security.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatsService {


    private final ChatsRepository chatRepository;
    private final UserService userService;
    private final ContactsService contactsService;
    private final ChatsMapper chatsMapper;

    // поиск чата по Id
    public Chats getChatById(Long id){
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Чат не найден"));
    }

    // Проверка, что пользователь является создателем чата
    private void checkCreator(User currentUser, Chats chat) {
        boolean isCreator = chat.getCreatorId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (!isCreator && !isAdmin) {
            throw new RuntimeException("Только создатель чата или администратор могут выполнять эту операцию");
        }
    }

    //Выдать все чаты пользователя
    public List<ChatResponse> getChats(Authentication authentication) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        List<Chats> activeChats = chatRepository.findByUsersContainingAndStatus(currentUser, "Active");
        return activeChats.stream()
                .map(chatsMapper::toChatResponse)
                .collect(Collectors.toList());
    }

    // Показать список чатов для Администратора!
    public List<ChatResponse> getAllChats(Authentication authentication) {
        User user = userService.findUserByUsername(authentication.getName());
        List<Chats> allChats = chatRepository.findAll();
        return allChats.stream()
                .map(chatsMapper::toChatResponse)
                .collect(Collectors.toList());
    }

    // создание чата
    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
        User creator = userService.findUserByUsername(authentication.getName());
        System.out.println(creator.getUsername());
        List<User> users = new ArrayList<>();
        System.out.println(request.getUsersId());
        if (request.getUsersId() != null) {
            System.out.println(request.getUsersId());
            for (Long userId : request.getUsersId()) {
                System.out.println(userId);
                User user = userService.findUserById(userId);
                System.out.println("User found");
                if (contactsService.findContactsStatus(userId, creator.getId(),  "Ignore").isEmpty() &&
                        contactsService.isAcceptedContacts(creator.getId(), userId)){
                    System.out.println("User add");
                    users.add(user);
                }
            }
        }
        if (!users.contains(creator)) {
            users.add(creator);
        }
        Chats chat = Chats.builder()
                .name(request.getName())
                .creatorId(creator.getId())
                .status("Active")
                .users(users)
                .build();

        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }

    // переименовывание чата
    public ChatResponse renameChat(Authentication authentication, RenameChatRequest request){
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(request.getId());
        checkCreator(currentUser, chat);
        chat.setName(request.getNewName());
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }

    // добавление пользователей в чат
    public ChatResponse addUsersToChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(request.getId());

        List<User> currentParticipants = chat.getUsers();
        if (currentParticipants == null) {
            currentParticipants = new ArrayList<>();
        }
        if (currentParticipants.size() == 2) {
            CreateChatRequest newChatRequest = new CreateChatRequest();

            List<Long> participantsIds = currentParticipants.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (request.getUsersId() != null) {

                for (Long userId : request.getUsersId()) {

                    if (!participantsIds.contains(userId)) {
                        if (contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                            participantsIds.add(userId);
                        }
                    }
                }
            }
            newChatRequest.setName(authentication.getName());
            newChatRequest.setUsersId(participantsIds);
            return createChat(authentication, newChatRequest);

        } else {
            for (Long userId : request.getUsersId()) {
                boolean exists = currentParticipants.stream().anyMatch(u -> u.getId().equals(userId));
                if (!exists) {
                    User user = userService.findUserById(userId);
                    if (contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                        currentParticipants.add(user);
                    }
                }
            }
            chat.setUsers(currentParticipants);
            chatRepository.save(chat);
            return chatsMapper.toChatResponse(chat);
        }
    }

    // удаление пользователей в чат
    public ChatResponse removeUserFromChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(request.getId());

        checkCreator(currentUser, chat);

        if (request.getUsersId() == null || request.getUsersId().isEmpty()) {
            throw new RuntimeException("Не указаны пользователи для удаления");
        }

        List<User> users = chat.getUsers();
        if (users == null) {
            throw new RuntimeException("Список участников чата пуст");
        }

        users.removeIf(user ->
                request.getUsersId().contains(user.getId()) && !user.getId().equals(chat.getCreatorId())
        );

        chat.setUsers(users);
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }

    //Деактивировать чат (может только создатель чата)
    public ChatResponse deactivateChat(Authentication authentication, Long id){
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(id);

        checkCreator(currentUser, chat);
        chat.setStatus("Deleted");
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }

    //Активировать чат (может только Администратор)
    public ChatResponse activatedChat(Authentication authentication, Long id){
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(id);
        checkCreator(currentUser, chat);
        chat.setStatus("Active");
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }
}
