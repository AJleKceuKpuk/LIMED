package com.limed_backend.security.service;

import com.limed_backend.security.dto.Chat.*;
import com.limed_backend.security.dto.Chat.CreateChatEvent;
import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.mapper.ChatsMapper;
import com.limed_backend.security.entity.enums.ChatEventType;
import com.limed_backend.security.repository.ChatUserRepository;
import com.limed_backend.security.repository.ChatsRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final EntityManager entityManager;
    private final UserCacheService userCache;
    private final ChatsCacheService chatsCache;
    private final ApplicationEventPublisher eventPublisher;

    //==========================СПИСКИ ЧАТОВ===========================//

    //Общий чат
    public AllChatResponse findAllChat(){
        Chats allChat = chatsCache.findAllChat();
        return new AllChatResponse(allChat.getId(),
                allChat.getName(),
                allChat.getType());
    }

    //Для информации о чате в шапке чата, доступно лишь пользователем чата!
    public ChatResponse findChatById(Long id, Authentication authentication){
        Chats chat = chatsCache.findChatById(id);
        User user = userCache.findUserByUsername(authentication.getName());
        boolean isAdmin = userService.isAdmin(user);
        if (isMember(chat, user) || isAdmin){
            return chatsMapper.toChatResponse(chat);
        }else {
            return null;
        }
    }
    //Выдать все чаты текущего пользователя
    public List<ChatResponse> findAllChatsUser(Authentication authentication) {
        List<Chats> activeChats = chatsCache.getAllChatsUser(authentication);
        return activeChats.stream()
                .map(chatsMapper::toChatResponse)
                .collect(Collectors.toList());
    }

    // Поиск приватного чата между пользователями
    public Chats findPrivateChat(List<Long> usersId) {
        return chatRepository.findPrivateChat(usersId, 2L).orElse(null);
    }

    //TODO Вернуться когда буду делать админку
    // Показать список чатов пользователя для Администратора!
    public List<ChatResponse> findAllChatsUserForAdmin(Long userId, Authentication authentication) {
        User admin = userCache.findUserByUsername(authentication.getName());
        if (userService.isAdmin(admin)) {
            List<Chats> allChats = chatsCache.getAllChatsByAdmin(userId);
            return allChats.stream()
                    .map(chatsMapper::toChatResponse)
                    .collect(Collectors.toList());
        }
        return null;
    }

    //========================УПРАВЛЕНИЕ ЧАТАМИ=============================//

    //создание чата
    @Transactional
    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
        User creator = userCache.findUserByUsername(authentication.getName());
        String type = request.getType();
        String name = request.getName();
        Set<Long> usersId = new HashSet<>();
        if (request.getUsersId() != null) {
            usersId.addAll(request.getUsersId());
        }
        usersId.add(creator.getId());

        List<User> users = new ArrayList<>();
        for (Long userId : usersId) {
            User user = userCache.findUserById(userId);
            if (type.equals("PRIVATE")) {
                Chats existingChat = findPrivateChat(usersId.stream().toList());
                if (existingChat != null) {
                    return chatsMapper.toChatResponse(existingChat);
                }
                if (!userId.equals(creator.getId()) &&
                        contactsService.findDirectStatus(userId, creator.getId(), "Ignore").isEmpty()) {
                    users.add(user);
                } else if (userId.equals(creator.getId())) {
                    users.add(user);
                }
            } else if (type.equals("GROUP")){
                if (name == null || name.isEmpty()){
                    name = creator.getUsername() + " chat";
                }
                if (contactsService.findDirectStatus(userId, creator.getId(), "Ignore").isEmpty() &&
                        (userId.equals(creator.getId()) || contactsService.isAcceptedContacts(creator.getId(), userId))) {
                    users.add(user);
                }
            }
        }
        Chats chat = Chats.builder()
                .name(name)
                .creatorId(creator.getId())
                .type(type)
                .status("Active")
                .build();

        List<ChatUser> chatUsers = new ArrayList<>();
        for (User user : users) {
            User managedUser = entityManager.find(User.class, user.getId());
            ChatUser chatUser = new ChatUser();

            chatUser.setChat(chat);
            chatUser.setUser(managedUser);
            chatUser.setStatus("Active");
            chatUsers.add(chatUser);
        }
        chat.setChatUsers(chatUsers);
        chatRepository.save(chat);

        eventPublisher.publishEvent(new CreateChatEvent(ChatEventType.CHAT_CREATED, chat, creator));

        return chatsMapper.toChatResponse(chat);
    }

    //изменение названия чата
    @Transactional
    public ChatResponse renameChat(Authentication authentication, RenameChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());
        checkCreator(currentUser, chat);
        if (isAllChat(chat)){
            throw new ResourceNotFoundException();
        }
        if ("PRIVATE".equals(chat.getType())){
            throw new ResourceNotFoundException();
        }

        String oldName = chat.getName();
        chat.setName(request.getNewName());
        Chats updatedChat = chatRepository.save(chat);

        chatsCache.removeChatToCache(chat);
        chatsCache.addChatToCache(chat);

        // Создаём универсальное событие переименования
        CreateChatEvent event = new CreateChatEvent(ChatEventType.CHAT_RENAMED, updatedChat, currentUser);
        event.putPayload("oldName", oldName);
        eventPublisher.publishEvent(event);

        return chatsMapper.toChatResponse(updatedChat);
    }

    //Деактивировать чат (может только создатель чата)
    public ChatResponse deactivateChat(Authentication authentication, Long id){
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(id);
        checkCreator(currentUser, chat);
        if (chat.getId() == 1){
            throw new ResourceNotFoundException();
        }
        chat.setStatus("Deleted");
        chatRepository.save(chat);

        chatsCache.removeChatToCache(chat);

        return chatsMapper.toChatResponse(chat);
    }

    //TODO Реализовать когда буду делать админ контроллер
    // Активировать чат (может только Администратор)
    public ChatResponse activatedChat(Authentication authentication, Long id){
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(id);
        if (isAllChat(chat)){
            throw new ResourceNotFoundException();
        }
        chatsCache.removeChatToCache(chat);
        checkCreator(currentUser, chat);
        chat.setStatus("Active");
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }

    // добавление пользователей в чат
    @Transactional
    public ChatResponse addUsersToChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());

        if (isAllChat(chat)) {
            throw new ResourceNotFoundException();
        }

        if (!isMember(chat, currentUser)) {
            throw new ResourceNotFoundException();
        }

        if ("PRIVATE".equals(chat.getType())) {
            Set<Long> currentUserIds = chat.getChatUsers().stream()
                    .map(chatUser -> chatUser.getUser().getId())
                    .collect(Collectors.toSet());

            if (request.getUsersId() != null) {
                for (Long userId : request.getUsersId()) {
                    if (!currentUserIds.contains(userId)) {
                        if (userId.equals(currentUser.getId()) ||
                                contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                            currentUserIds.add(userId);
                        }
                    }
                }
            }
            CreateChatRequest newChatRequest = new CreateChatRequest();
            newChatRequest.setName(authentication.getName());
            newChatRequest.setUsersId(new ArrayList<>(currentUserIds));
            chatsCache.removeChatToCache(chat);
            chatsCache.addChatToCache(chat);
            return createChat(authentication, newChatRequest);
        } else if ("GROUP".equals(chat.getType())) {
            List<ChatUser> chatUsers = chat.getChatUsers();
            List<String> addedUsersNamesList = new ArrayList<>();

            for (Long userId : request.getUsersId()) {
                Optional<ChatUser> existingChatUser = chatUsers.stream()
                        .filter(cu -> cu.getUser().getId().equals(userId))
                        .findFirst();
                System.out.println(existingChatUser);
                if (existingChatUser.isPresent()) {
                    ChatUser chatUser = existingChatUser.get();
                    if (!"Active".equals(chatUser.getStatus())) {
                        chatUser.setStatus("Active");
                        chatUserRepository.save(chatUser);
                        addedUsersNamesList.add(chatUser.getUser().getUsername());
                        System.out.println(addedUsersNamesList);
                    }
                } else if (contactsService.isAcceptedContacts(currentUser.getId(), userId)) {
                    User user = userCache.findUserById(userId);
                    ChatUser newChatUser = new ChatUser();
                    newChatUser.setChat(chat);
                    newChatUser.setUser(user);
                    newChatUser.setStatus("Active");
                    chatUsers.add(newChatUser);
                    addedUsersNamesList.add(user.getUsername());
                }
            }

            chat.setChatUsers(chatUsers);
            chatRepository.save(chat);

            // Публикуем событие только если были добавлены/активированы пользователи
            if (!addedUsersNamesList.isEmpty()) {
                String addedUsersNames = String.join(", ", addedUsersNamesList);
                CreateChatEvent event = new CreateChatEvent(ChatEventType.CHAT_ADDUSER, chat, currentUser);
                event.putPayload("addedUsersNames", addedUsersNames);
                eventPublisher.publishEvent(event);
            }
            chatsCache.removeChatToCache(chat);
            chatsCache.addChatToCache(chat);
            return chatsMapper.toChatResponse(chat);
        }


        return null;
    }


    // удаление пользователей в чат
    @Transactional
    public ChatResponse removeUserFromChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());

        if (isAllChat(chat)) {
            throw new ResourceNotFoundException();
        }

        checkCreator(currentUser, chat);

        if (request.getUsersId() == null || request.getUsersId().isEmpty()) {
            throw new RuntimeException("Не указаны пользователи для удаления");
        }

        List<ChatUser> chatUsers = chat.getChatUsers();
        List<String> removedUsersNamesList = new ArrayList<>();

        for (ChatUser chatUser : chatUsers) {
            Long userId = chatUser.getUser().getId();
            if (request.getUsersId().contains(userId) && !userId.equals(chat.getCreatorId())) {
                chatUser.setStatus("deleted");
                chatUserRepository.save(chatUser);
                removedUsersNamesList.add(chatUser.getUser().getUsername());
            }
        }

        chatRepository.save(chat);
        chatsCache.removeChatToCache(chat);
        chatsCache.addChatToCache(chat);

        if (!removedUsersNamesList.isEmpty()) {
            String removedUsersNames = String.join(", ", removedUsersNamesList);
            CreateChatEvent event = new CreateChatEvent(ChatEventType.CHAT_REMOVEUSER, chat, currentUser);
            event.putPayload("removedUsersNames", removedUsersNames);
            eventPublisher.publishEvent(event);
        }

        return chatsMapper.toChatResponse(chat);
    }


    //метод выхода из чата
    @Transactional
    public String leaveChat(Authentication authentication, Long chatId) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(chatId);

        if (isAllChat(chat)) {
            throw new ResourceNotFoundException();
        }
        if (!isMember(chat, currentUser)) {
            throw new ResourceNotFoundException();
        }

        ChatUser chatUser = chat.getChatUsers()
                .stream()
                .filter(cu -> cu.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);

        if (chat.getCreatorId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException();
        }

        chatUser.setStatus("leave");
        chatUserRepository.save(chatUser);

        chatsCache.removeChatToCache(chat);
        chatsCache.addChatToCache(chat);

        CreateChatEvent event = new CreateChatEvent(ChatEventType.CHAT_LEAVE, chat, currentUser);
        event.putPayload("leftUserName", currentUser.getUsername());
        eventPublisher.publishEvent(event);

        return "Вы успешно вышли из чата!";
    }

    //=====================================================//
    //общая проверка
    private void checkCreator(User currentUser, Chats chat) {
        if (!isCreator(currentUser, chat) && !userService.isAdmin(currentUser)) {
            throw new ResourceNotFoundException();
        }
    }

    private boolean isAllChat(Chats chat){
        return chat.getId() == 1;
    }

    //проверка, что пользователь - создатель чата
    public boolean isCreator(User user, Chats chat){
        return chat.getCreatorId().equals(user.getId());
    }

    public boolean isMember(Chats chat, User user){
        return chat.getChatUsers()
                .stream()
                .anyMatch(chatUser -> chatUser.getUser().getId().equals(user.getId()));
    }

}
