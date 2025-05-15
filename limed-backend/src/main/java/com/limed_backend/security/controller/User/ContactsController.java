package com.limed_backend.security.controller.User;

import com.limed_backend.security.dto.Contact.FriendResponse;
import com.limed_backend.security.dto.Contact.ContactAddRequest;
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

    /** Список друзей пользователя*/
    @GetMapping("/contacts")
    public ResponseEntity<List<FriendResponse>> getContacts(Authentication authentication) {
        User user = userCache.findUserByUsername(authentication.getName());
        List<FriendResponse> friends = contactsService.findAcceptContacts(user);
        return ResponseEntity.ok(friends);
    }

    /** Список исходящих запросов*/
    @GetMapping("/contacts/pending")
    public ResponseEntity<List<NoFriendResponse>> getPendingContacts(Authentication authentication) {
        List<NoFriendResponse> pendingFriends = contactsService.findPendingContacts(authentication);
        return ResponseEntity.ok(pendingFriends);
    }

    /** Список входящих запросов*/
    @GetMapping("/contacts/invitation")
    public ResponseEntity<List<NoFriendResponse>> getInvitationContacts(Authentication authentication) {
        List<NoFriendResponse> invitationFriends = contactsService.findInviteContacts(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    /** Черный список*/
    @GetMapping("/contacts/ignore")
    public ResponseEntity<List<NoFriendResponse>> getIgnoreList(Authentication authentication) {
        List<NoFriendResponse> invitationFriends = contactsService.findIgnoreContacts(authentication);
        return ResponseEntity.ok(invitationFriends);
    }

    /** Добавление пользователя в друзья по его ID либо Username*/
    @PostMapping("/add-contact")
    public ResponseEntity<String> addContacts(Authentication authentication,
                                              ContactAddRequest request){
        System.out.println("/add contacts /id");
        String resultMessage = contactsService.addContacts(authentication, request);
        return ResponseEntity.ok(resultMessage);
    }

    /** Добавление пользователя в черный список по ID или Username*/
    @PostMapping("/ignore/{id}")
    public ResponseEntity<String> addContactsIgnore(Authentication authentication,
                                                    ContactAddRequest request) {
        System.out.println("/ignore /id");
        String resultMessage = contactsService.addIgnore(authentication, request);
        return ResponseEntity.ok(resultMessage);
    }

    /** Принятие запроса в друзья*/
    @PostMapping("/accept-contacts/{id}")
    public ResponseEntity<String> acceptContacts(Authentication authentication,
                                                 @PathVariable Long id){
        System.out.println("/accept contacts /id");
        String resultMessage = contactsService.acceptContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    /** Отказ от запроса в друзья*/
    @DeleteMapping("/cansel-contacts/{id}")
    public ResponseEntity<String> canselContacts(Authentication authentication,
                                                 @PathVariable Long id){
        System.out.println("/cansel contacts /id");
        String resultMessage = contactsService.cancelContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    /** Удаление пользователя из списка друзей*/
    @DeleteMapping("/delete-contacts/{id}")
    public ResponseEntity<String> deleteContacts(Authentication authentication,
                                                 @PathVariable Long id){
        System.out.println("/delete contacts /id");
        String resultMessage = contactsService.deleteContacts(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    /** Удаление пользователя из черного списка*/
    @DeleteMapping("/delete-ignore/{id}")
    public ResponseEntity<String> deleteIgnore(Authentication authentication,
                                               @PathVariable Long id){
        System.out.println("/delete ignore /id");
        String resultMessage = contactsService.deleteIgnore(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }
}
