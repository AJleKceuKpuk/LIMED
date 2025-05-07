package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Responses.*;
import com.limed_backend.security.dto.Requests.UpdateEmailRequest;
import com.limed_backend.security.dto.Requests.UpdatePasswordRequest;
import com.limed_backend.security.dto.Requests.UpdateUsernameRequest;
import com.limed_backend.security.dto.Responses.Contact.NoFriendResponse;
import com.limed_backend.security.dto.Responses.Contact.FriendResponse;
import com.limed_backend.security.dto.Responses.User.UserProfileResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.ContactsCacheService;
import com.limed_backend.security.service.ContactsService;
import com.limed_backend.security.service.UserCacheService;
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
    private final UserCacheService userCache;


    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        System.out.println("/profile");
        UserProfileResponse user = userService.getProfile(authentication);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-username")
    public ResponseEntity<TokenResponse> updateUsername(HttpServletRequest request, @RequestBody UpdateUsernameRequest userRequest,
                                                        Authentication authentication,
                                                        HttpServletResponse response) {
        System.out.println("/update-username");
        TokenResponse tokenResponse = userService.updateUsername(request, authentication.getName(), userRequest, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request,
                                              Authentication authentication) {
        System.out.println("/update-email");
        String result = userService.updateEmail(authentication.getName(), request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                 Authentication authentication) {
        System.out.println("/update-password");
        String resultMessage = userService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<FriendResponse>> getContacts(Authentication authentication) {
        User user = userCache.findUserByUsername(authentication.getName());
        List<FriendResponse> friends = contactsService.findAcceptContacts(user);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/contacts/pending")
    public ResponseEntity<List<NoFriendResponse>> getPendingContacts(Authentication authentication) {
        List<NoFriendResponse> pendingFriends = contactsService.findPendingContacts(authentication);
        return ResponseEntity.ok(pendingFriends);
    }

    @GetMapping("/contacts/invitation")
    public ResponseEntity<List<NoFriendResponse>> getInvitationContacts(Authentication authentication) {
        List<NoFriendResponse> invitationFriends = contactsService.findInviteContacts(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    @GetMapping("/contacts/ignore")
    public ResponseEntity<List<NoFriendResponse>> getIgnoreList(Authentication authentication) {
        List<NoFriendResponse> invitationFriends = contactsService.findIgnoreContacts(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    @PostMapping("/add-contacts/{id}")
    public ResponseEntity<String> addContacts(Authentication authentication,
                                            @PathVariable Long id){
        System.out.println("/add contacts /id");
        String resultMessage = contactsService.addContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/accept-contacts/{id}")
    public ResponseEntity<String> acceptContacts(Authentication authentication,
                                               @PathVariable Long id){
        System.out.println("/accept contacts /id");
        String resultMessage = contactsService.acceptContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/cansel-contacts/{id}")
    public ResponseEntity<String> canselContacts(Authentication authentication,
                                               @PathVariable Long id){
        System.out.println("/cansel contacts /id");
        String resultMessage = contactsService.cancelContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/delete-contacts/{id}")
    public ResponseEntity<String> deleteContacts(Authentication authentication,
                                                @PathVariable Long id){
        System.out.println("/delete contacts /id");
        String resultMessage = contactsService.deleteContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/ignore/{id}")
    public ResponseEntity<String> addContactsIgnore(Authentication authentication,
                                                    @PathVariable Long id){
        System.out.println("/ignore /id");
        String resultMessage = contactsService.addIgnore(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/delete-ignore/{id}")
    public ResponseEntity<String> deleteIgnore(Authentication authentication,
                                               @PathVariable Long id){
        System.out.println("/delete ignore /id");
        String resultMessage = contactsService.deleteIgnore(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

}
