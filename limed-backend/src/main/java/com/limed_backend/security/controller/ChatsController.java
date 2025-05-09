package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Chat.CreateChatRequest;
import com.limed_backend.security.dto.Requests.MessageRequest;
import com.limed_backend.security.dto.Chat.RenameChatRequest;
import com.limed_backend.security.dto.Chat.UsersChatRequest;
import com.limed_backend.security.dto.Chat.ChatResponse;
import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.ChatsService;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserCacheService;
import com.limed_backend.security.service.UserService;
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
    private final UserService userService;
    private final UserCacheService userCache;

    @GetMapping("/all")
    public ResponseEntity<List<ChatResponse>> getChats(Authentication authentication){
        return ResponseEntity.ok(chatsService.findAllChatsUser(authentication));
    }

    @PostMapping("/create")
    public ResponseEntity<ChatResponse> createChat(Authentication authentication,
                                                   @RequestBody CreateChatRequest request){
        ChatResponse chat = chatsService.createChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/rename")
    public ResponseEntity<ChatResponse> renameChat(Authentication authentication,
                                                       @RequestBody RenameChatRequest request){
        ChatResponse chat = chatsService.renameChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/add-user")
    public ResponseEntity<ChatResponse> addUsersToChat(Authentication authentication,
                                                       @RequestBody UsersChatRequest request){
        ChatResponse chat = chatsService.addUsersToChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/remove-user")
    public ResponseEntity<ChatResponse> removeUserFromChat(Authentication authentication,
                                                       @RequestBody UsersChatRequest request){
        ChatResponse chat = chatsService.removeUserFromChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/remove-chat/{id}")
    public ResponseEntity<ChatResponse> removeChat(Authentication authentication,
                                                           @PathVariable Long id){
        ChatResponse chat = chatsService.deactivateChat(authentication, id);
        return ResponseEntity.ok(chat);
    }



    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            Authentication authentication,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<MessageResponse> messages = messagesService.findMessagesFromChat(authentication, chatId, size, page);

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/create-message")
    public ResponseEntity<MessageResponse> createMessage(Authentication authentication,
                                         @RequestBody MessageRequest request){
        MessageResponse message = messagesService.createMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/edit-message")
    public ResponseEntity<MessageResponse> editMessage(Authentication authentication,
                                                       @RequestBody MessageRequest request) {
        MessageResponse message = messagesService.editMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/delete-message")
    public ResponseEntity<String> deleteMessage(Authentication authentication,
                                                       @RequestBody MessageRequest request) {
        String message = messagesService.deleteMessage(authentication, request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/unread")
    public ResponseEntity<Long> unreadMessages(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        Long unreadCount = messagesService.countUnreadMessages(user);
        return ResponseEntity.ok(unreadCount);
    }
}
