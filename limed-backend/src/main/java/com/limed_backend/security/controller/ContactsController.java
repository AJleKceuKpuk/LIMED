package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Contact.FriendResponse;
import com.limed_backend.security.dto.Contact.FriendUsernameRequest;
import com.limed_backend.security.dto.Contact.NoFriendResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.service.ContactsService;
import com.limed_backend.security.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ContactsController {

    private final ContactsService contactsService;
    private final UserCacheService userCache;

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

    @PostMapping("/add-contacts")
    public ResponseEntity<String> addContactsByUsername(Authentication authentication,
                                                        FriendUsernameRequest request){
        System.out.println("/add contacts username");
        String resultMessage = contactsService.addContactsByUsername(authentication, request);
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
