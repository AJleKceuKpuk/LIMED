package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.RenameChatRequest;
import com.limed_backend.security.dto.Requests.UsersChatRequest;
import com.limed_backend.security.dto.Responses.Chat.ChatResponse;
import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ChatsMapper;
import com.limed_backend.security.repository.ChatUserRepository;
import com.limed_backend.security.repository.ChatsRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    private final CacheManager cacheManager;
    private final EntityManager entityManager;
    private final UserCacheService userCache;
    private final ChatsCacheService chatsCache;

    //==========================СПИСКИ ЧАТОВ===========================//

    //Для информации о чате в шапке чата, доступно лишь пользователем чата!
    public ChatResponse findChatById(Long id, Authentication authentication){
        Chats chat = chatsCache.findChatById(id);
        User user = userCache.findUserByUsername(authentication.getName());
        boolean isMember = chat.getChatUsers()
                .stream()
                .anyMatch(chatUser -> chatUser.getUser().getId().equals(user.getId()));
        boolean isAdmin = userService.isAdmin(user);
        if (isMember || isAdmin){
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

    //Поиск приватного чата между пользователями
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


    //========================УПРАВЛЕНИЕ ЧАТАМИ=============================//

    //создание чата
    @Transactional
    public ChatResponse createChat(Authentication authentication, CreateChatRequest request) {
        User creator = userCache.findUserByUsername(authentication.getName());
        String type = request.getType();
        Set<Long> usersId = new HashSet<>();
        if (request.getUsersId() != null) {
            usersId.addAll(request.getUsersId());
        }
        usersId.add(creator.getId());

        List<User> users = new ArrayList<>();
        for (Long userId : usersId) {
            User user = userCache.findUserById(userId);
            if (type.equals("PRIVATE")) {
                if (!userId.equals(creator.getId()) &&
                        contactsService.findDirectStatus(userId, creator.getId(), "Ignore").isEmpty()) {
                    users.add(user);
                } else if (userId.equals(creator.getId())) {
                    users.add(user);
                }
            } else if (type.equals("GROUP")){
                if (contactsService.findDirectStatus(userId, creator.getId(), "Ignore").isEmpty() &&
                        (userId.equals(creator.getId()) || contactsService.isAcceptedContacts(creator.getId(), userId))) {
                    users.add(user);
                }
            }
        }

        Chats chat = Chats.builder()
                .name(request.getName())
                .creatorId(creator.getId())
                .type(type)
                .status("Active")
                .build();

        List<ChatUser> chatUsers = new ArrayList<>();
        // Здесь гарантируем, что для каждого user получаем управляемую сущность
        for (User user : users) {
            // Если user уже отсоединён, можно переподключить его, например:
            User managedUser = entityManager.find(User.class, user.getId());
            ChatUser chatUser = new ChatUser();
            chatUser.setChat(chat);
            chatUser.setUser(managedUser);
            chatUser.setStatus("Active");
            chatUsers.add(chatUser);
        }
        chat.setChatUsers(chatUsers);
        chatRepository.save(chat);
        return chatsMapper.toChatResponse(chat);
    }


    //изменение названия чата
    public ChatResponse renameChat(Authentication authentication, RenameChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());
        checkCreator(currentUser, chat);
        if ("PRIVATE".equals(chat.getType())){
            throw new ResourceNotFoundException("Изменить название приватного чата невозможно!");
        }
      //    deleteChatByIdCache(chat); //удаляем кэш по Id чата
        chat.setName(request.getNewName());
        Chats updatedChat = chatRepository.save(chat);
     //   addChatByIdCache(chat); //добавляем в кэш по Id чата
        return chatsMapper.toChatResponse(updatedChat);
    }

    //Деактивировать чат (может только создатель чата)
    public ChatResponse deactivateChat(Authentication authentication, Long id){
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(id);
        checkCreator(currentUser, chat);
        chat.setStatus("Deleted");
        chatRepository.save(chat);
     //   deleteChatByIdCache(chat);
        return chatsMapper.toChatResponse(chat);
    }

    //Активировать чат (может только Администратор)
    public ChatResponse activatedChat(Authentication authentication, Long id){
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(id);
     //   deleteChatByIdCache(chat);
        checkCreator(currentUser, chat);
        chat.setStatus("Active");
        chatRepository.save(chat);
      //  addChatByIdCache(chat);
        return chatsMapper.toChatResponse(chat);
    }

    //=====================================================//
    // добавление пользователей в чат
    public ChatResponse addUsersToChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());

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
                    User user = userCache.findUserById(userId);
                    ChatUser newChatUser = new ChatUser();

                    newChatUser.setChat(chat);
                    newChatUser.setUser(user);
                    newChatUser.setStatus("Active");
                    chatUsers.add(newChatUser);
                }
            }
           // deleteChatByIdCache(chat);
            chat.setChatUsers(chatUsers);

            chatRepository.save(chat);
           // addChatByIdCache(chat);
            return chatsMapper.toChatResponse(chat);
        }
        return null;
    }

    // удаление пользователей в чат
    public ChatResponse removeUserFromChat(Authentication authentication, UsersChatRequest request) {
        User currentUser = userCache.findUserByUsername(authentication.getName());
        Chats chat = chatsCache.findChatById(request.getId());

        checkCreator(currentUser, chat);

        if (request.getUsersId() == null || request.getUsersId().isEmpty()) {
            throw new RuntimeException("Не указаны пользователи для удаления");
        }

        List<ChatUser> chatUsers = chat.getChatUsers();

        chatUsers.forEach(chatUser -> {
            Long userId = chatUser.getUser().getId();
            if (request.getUsersId().contains(userId) && !userId.equals(chat.getCreatorId())) {
                chatUser.setStatus("deleted");
                chatUserRepository.save(chatUser);
            }
        });

        chatRepository.save(chat);

        //deleteChatByIdCache(chat);
       // addChatByIdCache(chat);

        return chatsMapper.toChatResponse(chat);
    }

    //метод выхода из чата
    public ChatResponse leaveChat(Authentication authentication, Long chatId) {
        User currentUser = userCache.findUserByUsername(authentication.getName());

        Chats chat = chatsCache.findChatById(chatId);

        ChatUser chatUser = chat.getChatUsers()
                .stream()
                .filter(cu -> cu.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of chat with id " + chatId));

        if (chat.getCreatorId().equals(currentUser.getId())) {
            throw new RuntimeException("Создатель чата не может выйти из чата");
        }
        //deleteChatByIdCache(chat);
        chatUser.setStatus("leave");

        chatUserRepository.save(chatUser);
       // addChatByIdCache(chat);
        return chatsMapper.toChatResponse(chat);
    }


    //=====================================================//
    //общая проверка
    private void checkCreator(User currentUser, Chats chat) {
        if (!isCreator(currentUser, chat) && !userService.isAdmin(currentUser)) {
            throw new RuntimeException("Только создатель чата или администратор могут выполнять эту операцию");
        }
    }

    //проверка, что пользователь - создатель чата
    public boolean isCreator(User user, Chats chat){
        return chat.getCreatorId().equals(user.getId());
    }


}
