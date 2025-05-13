package com.limed_backend.security.service;

import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatsCacheService {

    private final ChatsRepository chatsRepository;
    private final UserCacheService userCache;
    private final CacheManager cacheManager;

    //Поиск общего чата (доступен всем)
    @Cacheable(value = "chat", key = "'all'")
    public Chats findAllChat(){
        return chatsRepository.findById(1L)
                .orElseThrow(ResourceNotFoundException::new);
    }

    //поиск чата по id (для информации в профиле чата)
    @Cacheable(value = "chat", key = "#id")
    public Chats findChatById(Long id){
        return chatsRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    //поиск всех чатов пользователя, не удаленных
    @Cacheable(value = "chats", key = "#authentication.name")
    public List<Chats> getAllChatsUser(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        return chatsRepository.findActiveChatsByUser(user.getId())
                .orElseGet(ArrayList::new);
    }

    //поиск всех чатов пользователя для администратора
    @Cacheable(value = "chats-admin", key = "#authentication.name + '-' + #userId")
    public List<Chats> getAllChatsByAdmin(Long userId){
        return chatsRepository.findChatsByUser(userId)
                .orElseGet(ArrayList::new);
    }

    public void removeChatToCache(Chats chat) {
        // Очистка кэша "chat", где используются ключи: id чата и имя пользователя
        Cache chatCache = cacheManager.getCache("chat");
        if (chatCache != null) {
            chatCache.evict(chat.getId());
        } else {
            System.out.println("Кэш 'chat' не найден.");
        }

        // Очистка кэша "chats" для всех участников чата
        Cache chatsCache = cacheManager.getCache("chats");
        if (chatsCache != null) {
            if (chat != null && chat.getChatUsers() != null) {
                for (ChatUser chatUser : chat.getChatUsers()) {
                    User participant = chatUser.getUser();
                    if (participant != null && participant.getUsername() != null) {
                        chatsCache.evict(participant.getUsername());
                    }
                }
            } else {
                System.out.println("Чат или участники чата не заданы.");
            }
        } else {
            System.out.println("Кэш 'chats' не найден.");
        }

        Cache adminCache = cacheManager.getCache("chats-admin");
        if (adminCache != null) {
            try {
                adminCache.clear(); // Полная очистка кэша через публичный метод clear() интерфейса Cache
                System.out.println("Кэш 'chats-admin' успешно очищен.");
            } catch (Exception e) {
                System.out.println("Ошибка при очистке кэша 'chats-admin': " + e.getMessage());
            }
        } else {
            System.out.println("Кэш 'chats-admin' не найден.");
        }
    }

    public void addChatToCache(Chats chat) {
        Cache cache = cacheManager.getCache("chat");
        if (cache != null) {
            cache.put(chat.getId(), chat);
        }
    }
}
