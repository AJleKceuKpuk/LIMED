package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.RenameChatRequest;
import com.limed_backend.security.dto.Requests.UsersChatRequest;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ChatsMapper;
import com.limed_backend.security.repository.ChatUserRepository;
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
    private final ChatUserRepository chatUserRepository;

    // поиск чата по Id
    public Chats getChatById(Long id){
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Чат не найден"));
    }

    //проверка, что пользователь - создатель чата
    public boolean isCreator(User user, Chats chat){
        return chat.getCreatorId().equals(user.getId());
    }

    //общая проверка
    private void checkCreator(User currentUser, Chats chat) {
        if (!isCreator(currentUser, chat) && userService.isAdmin(currentUser)) {
            throw new RuntimeException("Только создатель чата или администратор могут выполнять эту операцию");
        }
    }

    //Поиск приватного чата исходя из списка пользователей (приватный чат с пустым именем)
    public Chats findPrivateChat(List<Long> usersId) {
        Set<Long> requestedUserIds = new HashSet<>(usersId);
        List<Chats> privateChats = chatRepository.findByType("PRIVATE");

        Optional<Chats> privateChat = privateChats.stream()
                .filter(chat -> {
                    Set<Long> chatUserIds = chat.getChatUsers()
                            .stream()
                            .map(chatUser -> chatUser.getUser().getId())
                            .collect(Collectors.toSet());
                    return chatUserIds.equals(requestedUserIds);
                })
                .findFirst();
        return privateChat.orElse(null);
    }


    //Выдать все чаты текущего пользователя
    public List<ChatResponse> getChats(Authentication authentication) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        List<Chats> activeChats = chatRepository.findDistinctByChatUsersUserAndChatUsersStatus(currentUser, "Active");

        return activeChats.stream()
                .map(chatsMapper::toChatResponse)
                .collect(Collectors.toList());
    }


    // Показать список чатов для Администратора!
    public List<ChatResponse> getAllChats(Authentication authentication) {
        User admin = userService.findUserByUsername(authentication.getName());
        if (userService.isAdmin(admin)) {
            List<Chats> allChats = chatRepository.findAll();
            return allChats.stream()
                    .map(chatsMapper::toChatResponse)
                    .collect(Collectors.toList());
        }
        return null;
    }

    // Показать список чатов для Администратора любого пользователя
    public List<ChatResponse> getAllChatsByUser(Authentication authentication, Long id) {
        User admin = userService.findUserByUsername(authentication.getName());
        User user = userService.findUserById(id);

        if (userService.isAdmin(admin)) {
            List<Chats> userChats = chatRepository.findDistinctByChatUsersUser(user);
            return userChats.stream()
                    .map(chatsMapper::toChatResponse)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
        User creator = userService.findUserByUsername(authentication.getName());
        String type = request.getType();
        //добавляем всех пользователей в список
        Set<Long> usersId = new HashSet<>();
        if (request.getUsersId() != null) {
            usersId.addAll(request.getUsersId());
        }
        //не забываем про создателя
        usersId.add(creator.getId());

        List<User> users = new ArrayList<>();
        for (Long userId : usersId) {
            User user = userService.findUserById(userId);
            if (type.equals("PRIVATE")) {
                if (!userId.equals(creator.getId()) &&
                        contactsService.findContactsStatus(userId, creator.getId(), "Ignore").isEmpty()) {
                    users.add(user);
                } else if (userId.equals(creator.getId())) {
                    users.add(user);
                }
            } else if (type.equals("GROUP")){
                if (contactsService.findContactsStatus(userId, creator.getId(), "Ignore").isEmpty() &&
                        (userId.equals(creator.getId()) || contactsService.isAcceptedContacts(creator.getId(), userId))) {
                    users.add(user);
                }
            }
        }

        // Создаём объект чата
        Chats chat = Chats.builder()
                .name(request.getName())
                .creatorId(creator.getId())
                .type(type)
                .status("Active")
                .build();

        // Создаём список записей ChatUser для каждого участника
        List<ChatUser> chatUsers = new ArrayList<>();
        for (User user : users) {
            ChatUser chatUser = new ChatUser();
            chatUser.setChat(chat);
            chatUser.setUser(user);
            chatUser.setStatus("Active");
            chatUsers.add(chatUser);
        }
        chat.setChatUsers(chatUsers);

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

        if ("PRIVATE".equals(chat.getType())) {
            Set<Long> currentUserIds = chat.getChatUsers().stream()
                    .map(chatUser -> chatUser.getUser().getId())
                    .collect(Collectors.toSet());

            if (request.getUsersId() != null) {
                for (Long userId : request.getUsersId()) {
                    if (!currentUserIds.contains(userId)) {
                        if (userId.equals(currentUser.getId()) || contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                            currentUserIds.add(userId);
                        }
                    }
                }
            }

            CreateChatRequest newChatRequest = new CreateChatRequest();
            newChatRequest.setName(authentication.getName());
            newChatRequest.setUsersId(new ArrayList<>(currentUserIds));

            return createChat(authentication, newChatRequest);
        } else if ("GROUP".equals(chat.getType())) {
            List<ChatUser> chatUsers = chat.getChatUsers();
            if (chatUsers == null) {
                chatUsers = new ArrayList<>();
            }

            for (Long userId : request.getUsersId()) {
                boolean exists = chatUsers.stream()
                        .anyMatch(cu -> cu.getUser().getId().equals(userId));
                if (!exists && contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                    User user = userService.findUserById(userId);
                    ChatUser newChatUser = new ChatUser();

                    newChatUser.setChat(chat);
                    newChatUser.setUser(user);
                    newChatUser.setStatus("Active");
                    chatUsers.add(newChatUser);
                }
            }
            chat.setChatUsers(chatUsers);
            chatRepository.save(chat);
            return chatsMapper.toChatResponse(chat);
        }
        return null;
    }

    // удаление пользователей в чат
    public ChatResponse removeUserFromChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(request.getId());

        checkCreator(currentUser, chat);

        if (request.getUsersId() == null || request.getUsersId().isEmpty()) {
            throw new RuntimeException("Не указаны пользователи для удаления");
        }
        List<ChatUser> chatUsers = chat.getChatUsers();
        if (chatUsers == null || chatUsers.isEmpty()) {
            throw new RuntimeException("Список участников чата пуст");
        }
        chatUsers.removeIf(chatUser -> {
            Long userId = chatUser.getUser().getId();
            return request.getUsersId().contains(userId) && !userId.equals(chat.getCreatorId());
        });

        chat.setChatUsers(chatUsers);
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

    public ChatResponse leaveChat(Authentication authentication, Long chatId) {
        User currentUser = userService.findUserByUsername(authentication.getName());

        Chats chat = getChatById(chatId);

        ChatUser chatUser = chat.getChatUsers()
                .stream()
                .filter(cu -> cu.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of chat with id " + chatId));

        if (chat.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("Создатель чата не может выйти из чата");
        }

        chatUser.setStatus("leave");
        chatUserRepository.save(chatUser);

        // Возвращаем обновлённую информацию о чате
        return chatsMapper.toChatResponse(chat);
    }

    public ChatResponse removeFromChat(Authentication authentication, Long chatId, Long userId){
        User currentUser = userService.findUserByUsername(authentication.getName());
        Chats chat = getChatById(chatId);
        checkCreator(currentUser, chat);

        ChatUser chatUser = chat.getChatUsers()
                .stream()
                .filter(cu -> cu.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of chat with id " + chatId));

        chatUser.setStatus("deleted");
        chatUserRepository.save(chatUser);

        // Возвращаем обновлённую информацию о чате
        return chatsMapper.toChatResponse(chat);
    }

}
