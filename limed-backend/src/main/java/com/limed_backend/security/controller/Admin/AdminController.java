package com.limed_backend.security.controller.Admin;

import com.limed_backend.security.dto.Chat.RenameChatRequest;
import com.limed_backend.security.dto.Chat.ChatResponse;
import com.limed_backend.security.dto.Message.MessageResponse;
import com.limed_backend.security.dto.Sanction.CreateSanctionRequest;
import com.limed_backend.security.dto.Sanction.DeleteSanctionRequest;
import com.limed_backend.security.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final ChatsService chatsService;
    private final MessagesService messagesService;
    private final SanctionService sanctionService;


    @PostMapping("/give-sanction")
    public ResponseEntity<String> giveSanction(@RequestBody CreateSanctionRequest request,
                                               Authentication authentication){
        String result = sanctionService.giveSanction(request, authentication);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unsanctioned")
    public ResponseEntity<String> unsanctioned(@RequestBody DeleteSanctionRequest request,
                                               Authentication authentication){
        String result = sanctionService.unsanctioned(request, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chats/user={id}")
    public List<ChatResponse> getAllChats(Authentication authentication, @PathVariable Long id){
        return chatsService.findAllChatsUserForAdmin(id, authentication);
    }

    @PostMapping("/chats/activate/{id}")
    public ChatResponse activateChat(Authentication authentication,
                                            @PathVariable Long id){
        return chatsService.activatedChat(authentication, id);
    }

    @PostMapping("/chats/deactivate/{id}")
    public ResponseEntity<ChatResponse> removeChat(Authentication authentication,
                                                   @PathVariable Long id){
        ChatResponse chat = chatsService.deactivateChat(authentication, id);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/chats/rename")
    public ResponseEntity<ChatResponse> renameChat(Authentication authentication,
                                                   @RequestBody RenameChatRequest request){
        ChatResponse chat = chatsService.renameChat(authentication, request);
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/chat={chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            Authentication authentication,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        System.out.println("/chat id messages");
        Page<MessageResponse> messages = messagesService.findMessagesFromChat(authentication, chatId, size, page);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<Page<MessageResponse>> getAllMessagesFromUser(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){

        Page<MessageResponse> messages = messagesService.findMessagesFromUser(authentication, id, size, page);
        return ResponseEntity.ok(messages);
    }
}