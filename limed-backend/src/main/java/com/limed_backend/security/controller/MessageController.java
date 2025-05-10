package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Message.MessageRequest;
import com.limed_backend.security.dto.Message.MessageResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class MessageController {

    private final MessagesService messagesService;
    private final UserCacheService userCache;

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
