package com.limed_backend.security.service;

import com.limed_backend.security.dto.Message.MessageRequest;
import com.limed_backend.security.dto.Chat.CreateChatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final MessagesService messageService;

    @TransactionalEventListener(condition = "#createChatEvent.eventType.name() == 'CHAT_CREATED'")
    public void ChatCreated(CreateChatEvent createChatEvent) {
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setChatId(createChatEvent.getChat().getId());
        messageRequest.setContent(createChatEvent.getUser().getUsername() + " создал новый чат");
        messageService.createSystemMessage(messageRequest);
    }

    @TransactionalEventListener(condition = "#createChatEvent.eventType.name() == 'CHAT_RENAMED'")
    public void chatRenamed(CreateChatEvent createChatEvent) {
        String oldName = (String) createChatEvent.getPayload().get("oldName");
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setChatId(createChatEvent.getChat().getId());
        messageRequest.setContent(String.format("%s переименовал чат с '%s' на '%s'",
                createChatEvent.getUser().getUsername(), oldName, createChatEvent.getChat().getName()));
        messageService.createSystemMessage(messageRequest);
    }

    @TransactionalEventListener(condition = "#createChatEvent.eventType.name() == 'CHAT_ADDUSER'")
    public void addUserToChatEvent(CreateChatEvent createChatEvent) {
        String addedUsersNames = (String) createChatEvent.getPayload().get("addedUsersNames");
        String content = String.format("%s добавил пользователей: %s",
                createChatEvent.getUser().getUsername(), addedUsersNames);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setChatId(createChatEvent.getChat().getId());
        messageRequest.setContent(content);
        messageService.createSystemMessage(messageRequest);
    }

    @TransactionalEventListener(condition = "#chatEvent.eventType.name() == 'CHAT_REMOVEUSER'")
    public void removeUserFromChatEvent(CreateChatEvent chatEvent) {
        String removedUsersNames = (String) chatEvent.getPayload().get("removedUsersNames");
        String content = String.format("%s удалил(а) пользователей: %s",
                chatEvent.getUser().getUsername(), removedUsersNames);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setChatId(chatEvent.getChat().getId());
        messageRequest.setContent(content);
        messageService.createSystemMessage(messageRequest);
    }

    @TransactionalEventListener(condition = "#chatEvent.eventType.name() == 'CHAT_LEAVE'")
    public void leaveChatEventListener(CreateChatEvent chatEvent) {
        String leftUserName = (String) chatEvent.getPayload().get("leftUserName");
        if (leftUserName == null || leftUserName.trim().isEmpty()) {
            return;
        }
        String content = String.format("%s покинул(а) чат", leftUserName);
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setChatId(chatEvent.getChat().getId());
        messageRequest.setContent(content);
        System.out.println("Системное сообщение: " + messageRequest.getContent());
        messageService.createSystemMessage(messageRequest);
    }
}

