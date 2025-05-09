package com.limed_backend.security.websocket;

import com.limed_backend.security.dto.Requests.MessageRequest;
import com.limed_backend.security.dto.Requests.UserStatusRequest;
import com.limed_backend.security.dto.Chat.ChatEvent;
import com.limed_backend.security.dto.Contact.FriendResponse;
import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.entity.Chats;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final UserService userService;
    private final ConnectionService connectionService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessagesService messagesService;
    private final ChatsCacheService chatsCache;
    private final UserCacheService userCache;
    private final ContactsService contactsService;


    // Клиент отправляет обновление статуса на адрес /ws/online/update
    @MessageMapping("/online/update")
    public void updateUserStatus(UserStatusRequest message) {
        User user = userCache.findUserById(message.getUserId());
        connectionService.updateLastUserActivity(message.getUserId());
        userService.updateUserStatus(message.getUserId(), message.getStatus());

        List<FriendResponse> allContacts = contactsService.findAcceptContacts(user);
        UserStatusRequest update = new UserStatusRequest(message.getUserId(), message.getStatus());
        simpMessagingTemplate.convertAndSend("/ws/online/" + user.getUsername(), update);
        for (FriendResponse friend : allContacts) {
            String destination = "/ws/online/" + friend.getUsername();
            simpMessagingTemplate.convertAndSend(destination, update);
        }
    }



    @MessageMapping("/chat/send")
    public void sendMessage(Authentication authentication, MessageRequest request) {
        MessageResponse messageResponse = messagesService.createMessage(authentication, request);

        ChatEvent<MessageResponse> event = new ChatEvent<>("create", messageResponse);
        String destination = "/ws/chat/" + request.getChatId();
        simpMessagingTemplate.convertAndSend(destination, event);

        Chats chat = chatsCache.findChatById(request.getChatId());
        connectionService.checkUnreadMessagesForChat(chat);
    }

    @MessageMapping("/chat/edit")
    public void editMessage(Authentication authentication, MessageRequest request) {
        MessageResponse messageResponse = messagesService.editMessage(authentication, request);

        ChatEvent<MessageResponse> event = new ChatEvent<>("edit", messageResponse);
        String destination = "/ws/chat/" + messageResponse.getChatId();
        simpMessagingTemplate.convertAndSend(destination, event);
    }

    @MessageMapping("/chat/delete")
    public void deleteMessage(Authentication authentication, MessageRequest request) {
        messagesService.deleteMessage(authentication, request);

        ChatEvent<MessageResponse> event = new ChatEvent<>("delete", null);
        String destination = "/ws/chat/" + request.getChatId();

        simpMessagingTemplate.convertAndSend(destination, event);
    }

    @MessageMapping("/chat/view")
    public void viewMessage(Authentication authentication, MessageRequest request) {

        MessageResponse messageResponse = messagesService.viewMessage(authentication, request);
        ChatEvent<MessageResponse> event = new ChatEvent<>("view", messageResponse);
        String destination = "/ws/chat/" + request.getChatId();

        simpMessagingTemplate.convertAndSend(destination, event);

    }
}