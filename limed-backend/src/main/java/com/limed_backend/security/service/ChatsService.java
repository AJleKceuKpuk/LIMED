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

import java.util.*;
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

    //Поиск приватного чата исходя из списка пользователей (приватный чат с пустым именем)
    public Chats getPrivateChat(List<Long> usersId) {
        Set<Long> requestedUserIds = new HashSet<>(usersId);
        List<Chats> privateChats = chatRepository.findByNameIsNull();

        Optional<Chats> privateChat = privateChats.stream()
                .filter(chat -> {
                    Set<Long> chatUserIds = chat.getUsers()
                            .stream()
                            .map(User::getId)
                            .collect(Collectors.toSet());
                    boolean isMatch = chatUserIds.equals(requestedUserIds);
                    return isMatch;
                })
                .findFirst();
        return privateChat.orElse(null);
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
//    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
//        User creator = userService.findUserByUsername(authentication.getName());
//        System.out.println(creator.getUsername());
//        List<User> users = new ArrayList<>();
//        System.out.println(request.getUsersId());
//        if (request.getUsersId() != null) {
//            System.out.println(request.getUsersId());
//            for (Long userId : request.getUsersId()) {
//                System.out.println(userId);
//                User user = userService.findUserById(userId);
//                System.out.println("User found");
//                if (contactsService.findContactsStatus(userId, creator.getId(),  "Ignore").isEmpty() &&
//                        contactsService.isAcceptedContacts(creator.getId(), userId)){
//                    System.out.println("User add");
//                    users.add(user);
//                }
//            }
//        }
//        if (!users.contains(creator)) {
//            users.add(creator);
//        }
//        Chats chat = Chats.builder()
//                .name(request.getName())
//                .creatorId(creator.getId())
//                .status("Active")
//                .users(users)
//                .build();
//
//        chatRepository.save(chat);
//        return chatsMapper.toChatResponse(chat);
//    }

    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
        User creator = userService.findUserByUsername(authentication.getName());
        System.out.println("Creator: " + creator.getUsername());

        // Используем Set для устранения дублирования userId
        Set<Long> providedIds = new HashSet<>();
        if (request.getUsersId() != null) {
            providedIds.addAll(request.getUsersId());
            System.out.println("Provided user IDs: " + providedIds);
        }

        // Определяем, включён ли создатель в список переданных ID
        boolean creatorIncluded = providedIds.contains(creator.getId());
        // Если создателя нет, то итоговых участников будет на 1 больше, так как он добавляется потом
        int totalParticipants = creatorIncluded ? providedIds.size() : providedIds.size() + 1;

        // Если итоговых участников ровно 2, то это личный чат
        boolean isPrivateChat = (totalParticipants == 2);
        System.out.println("Is private chat: " + isPrivateChat + ", totalParticipants: " + totalParticipants);

        List<User> users = new ArrayList<>();

        // Проходим по переданным идентификаторам для добавления участников
        for (Long userId : providedIds) {
            // Если встречается идентификатор создателя, его проверять не обязательно — позже он гарантированно будет добавлен
            if (userId.equals(creator.getId())) {
                continue;
            }
            User user = userService.findUserById(userId);
            System.out.println("Processing user: " + user.getUsername() + " (" + user.getId() + ")");
            if (isPrivateChat) {
                // Для личного чата проверяем только условие игноров: участник не игнорирует создателя
                if (contactsService.findContactsStatus(userId, creator.getId(), "Ignore").isEmpty()) {
                    System.out.println("User " + user.getUsername() + " passed ignore check (private chat).");
                    users.add(user);
                } else {
                    System.out.println("User " + user.getUsername() + " is ignoring creator. Not added (private chat).");
                }
            } else {
                // Для группового чата проверяем оба условия: отсутствие игнорирования и подтверждённое добавление (accepted)
                if (contactsService.findContactsStatus(userId, creator.getId(), "Ignore").isEmpty() &&
                        contactsService.isAcceptedContacts(creator.getId(), userId)) {
                    System.out.println("User " + user.getUsername() + " added to group chat.");
                    users.add(user);
                } else {
                    System.out.println("User " + user.getUsername() + " was not added due to ignore/accepted check (group chat).");
                }
            }
        }

        // Если создатель еще не добавлен, добавляем его
        if (!users.contains(creator)) {
            users.add(creator);
            System.out.println("Creator added to chat participants.");
        }

        Chats chat = Chats.builder()
                .name(request.getName())
                .creatorId(creator.getId())
                .status("Active")
                .users(users)
                .build();

        chatRepository.save(chat);
        System.out.println("Chat created with ID: " + chat.getId());
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
