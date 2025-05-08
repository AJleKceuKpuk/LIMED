package com.limed_backend.security.service;

import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
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

    @Cacheable(value = "chat", key = "#id")
    public Chats findChatById(Long id){
        return chatsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Чат не найден"));
    }

    @Cacheable(value = "chats", key = "#authentication.name")
    public List<Chats> getAllChatsUser(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        return chatsRepository.findChatsByUserAndStatus(user.getId(), "Active")
                .orElseGet(ArrayList::new);
    }

    @Cacheable(value = "chats-admin", key = "#authentication.name")
    public List<Chats> getAllChatsByAdmin(Long userId){
        return chatsRepository.findChatsByUser(userId)
                .orElseGet(ArrayList::new);
    }
}
