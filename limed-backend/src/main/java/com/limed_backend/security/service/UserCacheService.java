package com.limed_backend.security.service;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    //получаем пользователя по имени и заносим в кэш
    @Cacheable(value = "userCache", key = "#username")
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    //получаем пользователя по Id и заносим в кэш
    @Cacheable(value = "userCache", key = "#id")
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    // добавляем в кэш пользователя
    public void addUserCache(User user) {
        Cache userCache = cacheManager.getCache("userCache");
        if (userCache != null) {
            userCache.put(user.getId(), user);
            userCache.put(user.getUsername(), user);
        }
    }

    //удаляем пользователя из кэша
    public void deleteUserCache(User user){
        Cache userCache = cacheManager.getCache("userCache");
        if (userCache != null) {
            userCache.evict(user.getId());
            userCache.evict(user.getUsername());
        }
    }


}