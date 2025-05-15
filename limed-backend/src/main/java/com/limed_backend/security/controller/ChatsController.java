package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Chat.*;
import com.limed_backend.security.dto.Message.MessageRequest;
import com.limed_backend.security.dto.Message.MessageResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.ChatsService;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserCacheService;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatsController {

    private final ChatsService chatsService;
    private final MessagesService messagesService;
    private final UserCacheService userCache;

    /** Все чаты пользователя */
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getAllChatByUser(Authentication authentication){
        List<ChatResponse> chats = chatsService.findAllChatsUser(authentication);
        return ResponseEntity.ok(chats);
    }

    /** Общий чат*/
    @GetMapping("/all")
    public ResponseEntity<AllChatResponse> getChats(){
        return ResponseEntity.ok(chatsService.findAllChat());
    }

    /** Поиск чата по Id*/
    @GetMapping("/chat={id}")
    public ResponseEntity<ChatResponse> getChatById(Authentication authentication, @PathVariable Long id){
        ChatResponse chat = chatsService.findChatById(id, authentication);
        return ResponseEntity.ok(chat);
    }

    /** Создание чата*/
    @PostMapping("/create")
    public ResponseEntity<ChatResponse> createChat(Authentication authentication,
                                                   @RequestBody CreateChatRequest request){
        ChatResponse chat = chatsService.createChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    /** Изменение имени чата*/
    @PostMapping("/rename")
    public ResponseEntity<ChatResponse> renameChat(Authentication authentication,
                                                       @RequestBody RenameChatRequest request){
        ChatResponse chat = chatsService.renameChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    /** Добавление пользователей в чат*/
    @PostMapping("/add-user")
    public ResponseEntity<ChatResponse> addUsersToChat(Authentication authentication,
                                                       @RequestBody UsersChatRequest request){
        ChatResponse chat = chatsService.addUsersToChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    /** Удаление пользователей из чата*/
    @PostMapping("/remove-user")
    public ResponseEntity<ChatResponse> removeUserFromChat(Authentication authentication,
                                                       @RequestBody UsersChatRequest request){
        ChatResponse chat = chatsService.removeUserFromChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    /** Удаление чата*/
    @PostMapping("/remove-chat/{id}")
    public ResponseEntity<ChatResponse> removeChat(Authentication authentication,
                                                           @PathVariable Long id){
        ChatResponse chat = chatsService.deactivateChat(authentication, id);
        return ResponseEntity.ok(chat);
    }

    /** Выход из чата*/
    @PostMapping("/leave-chat/{id}")
    public ResponseEntity<String> leaveFromChat(Authentication authentication,
                                                @PathVariable Long id){
        String result = chatsService.leaveChat(authentication, id);
        return ResponseEntity.ok(result);
    }

    /** Список всех сообщений постранично*/
    @GetMapping("/chat={chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(Authentication authentication,
                                                                 @PathVariable Long chatId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Page<MessageResponse> messages = messagesService.findMessagesFromChat(authentication, chatId, size, page);
        return ResponseEntity.ok(messages);
    }

    /** Создание сообщения*/
    @PostMapping("/chat={chatId}/create-message")
    public ResponseEntity<MessageResponse> createMessage(Authentication authentication,
                                                         @RequestBody MessageRequest request){
        MessageResponse message = messagesService.createMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    /** Изменение сообщения*/
    @PostMapping("/chat={chatId}/edit-message")
    public ResponseEntity<MessageResponse> editMessage(Authentication authentication,
                                                       @RequestBody MessageRequest request) {
        MessageResponse message = messagesService.editMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    /** Удаление сообщения*/
    @PostMapping("/chat={chatId}/delete-message")
    public ResponseEntity<String> deleteMessage(Authentication authentication,
                                                @RequestBody MessageRequest request) {
        String message = messagesService.deleteMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    /** Количество непрочитанных сообщений*/
    @GetMapping("/unread")
    public ResponseEntity<Long> unreadMessages(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        Long unreadCount = messagesService.countUnreadMessages(user);
        return ResponseEntity.ok(unreadCount);
    }
}
