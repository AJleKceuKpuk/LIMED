package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Requests.CreateChatRequest;
import com.limed_backend.security.dto.Requests.RenameChatRequest;
import com.limed_backend.security.dto.Requests.UsersChatRequest;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.service.ChatsService;
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

}
