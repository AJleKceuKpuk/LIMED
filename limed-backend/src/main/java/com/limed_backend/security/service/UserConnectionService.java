package com.limed_backend.security.service;

import com.limed_backend.security.controller.WebSocketController;
import com.limed_backend.security.dto.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserConnectionService {

    // Хранение времени последней активности: key = userId, value = timestamp (мс)
    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();


    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserService userService;


    public void updateUserActivity(Long userId) {
        System.out.println("updateUserActivity");
        userLastActivity.put(userId, System.currentTimeMillis());
    }

    @Scheduled(fixedDelay = 20000)
    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        long inactivityThreshold = 30000; // 60 секунд неактивности

        userLastActivity.forEach((userId, lastActivity) -> {
            if (currentTime - lastActivity > inactivityThreshold) {
                userService.updateStatus(userId, "away");
                System.out.println("User " + userId + " inactive for " +
                        (currentTime - lastActivity) + "ms. Setting status to AWAY.");

                UserStatus userStatus =  new UserStatus(userId, "away");

                messagingTemplate.convertAndSend("/topic/user/status", userStatus);

                // Удаляем пользователя из списка, чтобы не обновлять статус снова
                //userLastActivity.remove(userId);

                System.out.println("User " + userId + " 60 second dont say request. Away.");
            }
        });
    }
}