package com.limed_backend.security.service;

import com.limed_backend.security.dto.UserStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserConnectionService {

    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;


    @Autowired
    private SimpUserRegistry simpUserRegistry;

    public void updateLastUserActivity(Long userId) {
        System.out.println("updateUserActivity");
        userLastActivity.put(userId, System.currentTimeMillis());
    }

    public void removeUser(Long userId) {
        userLastActivity.remove(userId);
        System.out.println("User " + userId + " removed from activity tracking.");
    }


    @Scheduled(fixedDelay = 10000)
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        System.out.println("current time: " + currentTime);
        long inactivityThreshold = 30000; // 60 секунд неактивности

        userLastActivity.forEach((userId, lastActivityMillis) -> {
            System.out.println("Checking user id: " + userId);
            if (currentTime - lastActivityMillis > inactivityThreshold) {
                // Обновляем статус пользователя на "away"
                userService.updateOnlineStatus(userId, "away");

                // Преобразуем сохранённое время последней активности в LocalDateTime
                LocalDateTime recordedLastActivity = java.time.Instant
                        .ofEpochMilli(lastActivityMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();

                // Записываем в базу дату и время последнего реального действия пользователя,
                // вместо того чтобы записывать текущее время
                userService.updateLastActivity(userId, recordedLastActivity);

                UserStatusRequest statusUpdate = new UserStatusRequest(userId, "away");
                messagingTemplate.convertAndSend("/ws/online/users", statusUpdate);
                System.out.println("User " + userId + " inactive for "
                        + (currentTime - lastActivityMillis) + " ms. Status set to AWAY.");
            }
        });
    }
}
