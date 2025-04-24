package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.UserStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;


    //Добавляем дату последней активности, и имя пользователя в Map
    public void updateLastUserActivity(Long userId) {
        userLastActivity.put(userId, System.currentTimeMillis());
    }

    //Удаляем User из Map
    public void removeUser(Long userId) {
        userLastActivity.remove(userId);
    }

    //Запускается раз в 30 секунд
    @Scheduled(fixedDelay = 30000)
    public void activityCheck() {
        long currentTime = System.currentTimeMillis();
        long inactivityThreshold = 60000;

        userLastActivity.forEach((userId, lastActivityMillis) -> { //проходим по всей Map и смотрим юзера на последнюю активность
            if (currentTime - lastActivityMillis > inactivityThreshold) {
                userService.updateUserStatus(userId, "away");
                LocalDateTime recordedLastActivity = java.time.Instant
                        .ofEpochMilli(lastActivityMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
                userService.updateLastActivity(userId, recordedLastActivity);
                UserStatusRequest statusUpdate = new UserStatusRequest(userId, "away");
                messagingTemplate.convertAndSend("/ws/online/users/" + userId , statusUpdate); //возврат ответа клиенту
                System.out.println("User " + userId + " inactive for "
                        + (currentTime - lastActivityMillis) + " ms. Status set to AWAY.");
            }
        });
    }
}
