package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.MessageRequest;
import com.limed_backend.security.dto.Requests.RenameChatRequest;
import com.limed_backend.security.dto.Requests.UsersChatRequest;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.entity.Messages;
import com.limed_backend.security.mapper.MessageMapper;
import com.limed_backend.security.repository.MessageRepository;
import com.limed_backend.security.service.ChatsService;
import com.limed_backend.security.service.MessagesService;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatsController {

    private final ChatsService chatsService;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessagesService messagesService;

    @GetMapping("/all")
    public List<ChatResponse> getChats(Authentication authentication){
        return chatsService.getChats(authentication);
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
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Messages> messagesPage = messageRepository.findByChatIdOrderBySendTimeDesc(chatId, pageable);

        // Преобразование сущностей Message в DTO MessageResponse
        Page<MessageResponse> responsePage = messagesPage.map(messageMapper::toMessageResponse);

        return ResponseEntity.ok(responsePage);
    }

    @PostMapping("/chat={chatId}/create-message")
    public MessageResponse createMessage(Authentication authentication,
                                                         @PathVariable Long chatId,
                                                         @RequestBody MessageRequest request){
        System.out.println("create-message");
        return messagesService.createMessage(authentication, chatId, request);
    }

}
