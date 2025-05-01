package com.limed_backend.security.websocket;

import com.limed_backend.security.dto.Requests.UserStatusRequest;
import com.limed_backend.security.entity.ChatUser;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final Map<Long, Long> userLastActivity = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final MessagesService messagesService;


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

    public void checkUnreadMessagesForChat(Chats chat) {
        List<ChatUser> chatUsers = chat.getChatUsers();

        for (ChatUser chatUser : chatUsers) {
            Long userId = chatUser.getUser().getId();

            if (userLastActivity.containsKey(userId)) {
                User user = userService.findUserById(userId);
                Long unreadCount = messagesService.countUnreadMessages(user);
                messagingTemplate.convertAndSend("/ws/unread/" + userId, unreadCount);

                System.out.println("User " + userId + " has " + unreadCount + " unread messages in chat " + chat.getId());
            }
        }
    }
}
