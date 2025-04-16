package com.limed_backend.security.websocket;

import com.limed_backend.security.dto.UserStatusRequest;
import com.limed_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserConnectionService {

    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private SimpUserRegistry simpUserRegistry;

    //Добавляем дату последней активности, и имя пользователя в Map
    public void updateLastUserActivity(Long userId) {
        userLastActivity.put(userId, System.currentTimeMillis());
    }

    //Удаляем User из Map
    public void removeUser(Long userId) {
        userLastActivity.remove(userId);
    }


    //Запускается раз в 30 секунд
    @Scheduled(fixedDelay = 10000)
    public void activityCheck() {
        long currentTime = System.currentTimeMillis(); //получаем текущее время
        long inactivityThreshold = 20000;

        userLastActivity.forEach((userId, lastActivityMillis) -> { //проходим по всей Map и смотрим юзера на последнюю активность
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
                messagingTemplate.convertAndSend("/ws/online/users/" + userId , statusUpdate);
                System.out.println("User " + userId + " inactive for "
                        + (currentTime - lastActivityMillis) + " ms. Status set to AWAY.");
            }
        });
    }
}
