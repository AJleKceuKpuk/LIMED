package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Chat.*;
import com.limed_backend.security.dto.Message.MessageRequest;
import com.limed_backend.security.dto.Message.MessageResponse;
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


    //Все чаты пользователя
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getAllChatByUser(Authentication authentication){
        List<ChatResponse> chats = chatsService.findAllChatsUser(authentication);
        return ResponseEntity.ok(chats);
    }

    //общий чат
    @GetMapping("/all")
    public ResponseEntity<AllChatResponse> getChats(){
        return ResponseEntity.ok(chatsService.findAllChat());
    }

    //чат по id
    @GetMapping("/chat={id}")
    public ResponseEntity<ChatResponse> getChatById(Authentication authentication, @PathVariable Long id){
        ChatResponse chat = chatsService.findChatById(id, authentication);
        return ResponseEntity.ok(chat);
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

    @PostMapping("/leave-chat/{id}")
    public ResponseEntity<String> leaveFromChat(Authentication authentication,
                                                @PathVariable Long id){
        String result = chatsService.leaveChat(authentication, id);
        return ResponseEntity.ok(result);
    }


}
