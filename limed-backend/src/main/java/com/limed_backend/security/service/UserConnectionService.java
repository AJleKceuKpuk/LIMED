package com.limed_backend.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserConnectionService {

    // Хранение времени последней активности: key = userId, value = timestamp (мс)
    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    public void updateUserActivity(Long userId) {
        System.out.println("updateUserActivity");
        userLastActivity.put(userId, System.currentTimeMillis());
    }


    @Scheduled(fixedDelay = 60000)
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        long inactivityThreshold = 60000; // 60 секунд неактивности

        userLastActivity.forEach((userId, lastActivity) -> {
            if (currentTime - lastActivity > inactivityThreshold) {
                // Пользователь не активен. Обновляем статус даже если disconnect не произошло.
                userService.updateOnlineStatus(userId, false);
                // Удаляем пользователя из списка, чтобы не обновлять статус снова
                userLastActivity.remove(userId);
                System.out.println("User " + userId + " 60 second dont say request. OFFLINE.");
            }
        });
    }
}