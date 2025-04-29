package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Requests.*;
import com.limed_backend.security.dto.Responses.ChatResponse;
import com.limed_backend.security.dto.Responses.MessageResponse;
import com.limed_backend.security.dto.Responses.UserResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.mapper.ChatsMapper;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.AdminService;
import com.limed_backend.security.service.ChatsService;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AdminService adminService;
    private final UserService userService;
    private final ChatsService chatsService;
    private final MessagesService messagesService;

    @GetMapping("/get-allusers")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.findUserById(id);
        UserResponse userResponse = userMapper.toUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/edit-username/{id}")
    public ResponseEntity<String> editUsername(@RequestBody UpdateUsernameRequest request,
                                              @PathVariable Long id) {
        String result = adminService.editUsername(request, id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit-email/{id}")
    public ResponseEntity<String> editEmail(@RequestBody UpdateEmailRequest request,
                                               @PathVariable Long id) {
        String result = adminService.editEmail(request, id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit-role/{id}")
    public ResponseEntity<String> editRole(@RequestBody UpdateRoleRequest request,
                                           @PathVariable Long id) {
        String result = adminService.editRole(request, id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/give-blocked")
    public ResponseEntity<String> giveMuted(@RequestBody GiveBlockRequest request,
                                            Authentication authentication){
        String result = adminService.giveBlock(request, authentication);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unblock")
    public ResponseEntity<String> unblock(@RequestBody UnblockRequest request,
                                          Authentication authentication){
        String result = adminService.unblock(request, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chats")
    public List<ChatResponse> getAllChats(Authentication authentication){
        return chatsService.getAllChats(authentication);
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

        Page<MessageResponse> messages = messagesService.getMessagesFromChat(authentication, chatId, size, page);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<Page<MessageResponse>> getAllMessagesFromUser(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){

        Page<MessageResponse> messages = messagesService.getMessagesFromUser(authentication, id, size, page);
        return ResponseEntity.ok(messages);
    }
}