package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Responses.*;
import com.limed_backend.security.dto.Requests.UpdateEmailRequest;
import com.limed_backend.security.dto.Requests.UpdatePasswordRequest;
import com.limed_backend.security.dto.Requests.UpdateUsernameRequest;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.ContactsService;
import com.limed_backend.security.service.MessagesService;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final ContactsService contactsService;
    private final UserService userService;
    private final MessagesService messagesService;


    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = userService.findUserByUsername(authentication.getName());
        return ResponseEntity.ok(new UserProfileResponse(user.getId(),user.getUsername(), user.getEmail(), user.getDateRegistration()));
    }

    @PutMapping("/update-username")
    public ResponseEntity<TokenResponse> updateUsername(HttpServletRequest request, @RequestBody UpdateUsernameRequest userRequest,
                                                        Authentication authentication,
                                                        HttpServletResponse response) {
        TokenResponse tokenResponse = userService.updateUsername(request, authentication.getName(), userRequest, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request,
                                              Authentication authentication) {
        String result = userService.updateEmail(authentication.getName(), request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                 Authentication authentication) {
        String resultMessage = userService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactsResponse>> getContacts(Authentication authentication) {
        List<ContactsResponse> friends = contactsService.getContacts(authentication);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/contacts/pending")
    public ResponseEntity<List<ContactsPendingResponse>> getPendingContacts(Authentication authentication) {
        List<ContactsPendingResponse> pendingFriends = contactsService.getPendingContacts(authentication);
        return ResponseEntity.ok(pendingFriends);
    }

    @GetMapping("/contacts/invitation")
    public ResponseEntity<List<ContactsPendingResponse>> getInvitationContacts(Authentication authentication) {
        List<ContactsPendingResponse> invitationFriends = contactsService.getInvitationContacts(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    @GetMapping("/contacts/ignore")
    public ResponseEntity<List<ContactsPendingResponse>> getIgnoreList(Authentication authentication) {
        List<ContactsPendingResponse> invitationFriends = contactsService.getIgnoreList(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    @PostMapping("/add-contacts/{id}")
    public ResponseEntity<String> addContacts(Authentication authentication,
                                            @PathVariable Long id){
        String resultMessage = contactsService.addContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/accept-contacts/{id}")
    public ResponseEntity<String> acceptContacts(Authentication authentication,
                                               @PathVariable Long id){
        String resultMessage = contactsService.acceptContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/cansel-contacts/{id}")
    public ResponseEntity<String> canselContacts(Authentication authentication,
                                               @PathVariable Long id){
        String resultMessage = contactsService.cancelContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/delete-contacts/{id}")
    public ResponseEntity<String> deleteContacts(Authentication authentication,
                                                @PathVariable Long id){
        String resultMessage = contactsService.deleteContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/ignore/{id}")
    public ResponseEntity<String> addContactsIgnore(Authentication authentication,
                                                    @PathVariable Long id){
        String resultMessage = contactsService.addIgnore(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/delete-ignore/{id}")
    public ResponseEntity<String> deleteIgnore(Authentication authentication,
                                               @PathVariable Long id){
        String resultMessage = contactsService.deleteIgnore(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/message-new")
    public ResponseEntity<Long> countMessage(Authentication authentication){
        Long result = messagesService.countUnreadMessages(authentication);
        return ResponseEntity.ok(result);
    }

}
