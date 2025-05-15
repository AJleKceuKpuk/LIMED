package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Support.*;
import com.limed_backend.security.service.SupportMessageService;
import com.limed_backend.security.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;
    private final SupportMessageService supportMessageService;

    @GetMapping
    public ResponseEntity<Page<SupportResponse>> getAllSupports(Authentication authentication,
                                                                SupportStatusRequest request,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size){
        Page<SupportResponse> supports = supportService.getAllSupportByUser(authentication, request, size, page);
        return ResponseEntity.ok(supports);
    }

    @GetMapping("/unread")
    public ResponseEntity<Boolean> hasUnreadSupportMessages(Authentication authentication){
        boolean result = supportService.hasUnreadSupportMessages(authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ticket={id}")
    public ResponseEntity<SupportResponse> getSupportById(Authentication authentication,
                                                          @PathVariable Long id){
        SupportResponse support = supportService.getSupportById(authentication, id);
        return ResponseEntity.ok(support);
    }

    @GetMapping("/ticket={id}/messages")
    public ResponseEntity<Page<SupportMessageResponse>> getMessagesFromTicket(Authentication authentication,
                                                                        @PathVariable Long id,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size){
        Page<SupportMessageResponse> messages = supportMessageService.getAllMessagesBySupport(authentication, id, size, page);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/create")
    public ResponseEntity<SupportResponse> createTicket(Authentication authentication,
                                                        SupportCreateRequest request){
        SupportResponse support = supportService.createSupport(authentication, request);
        return ResponseEntity.ok(support);

    }

    @PostMapping("/ticket={id}/create")
    public ResponseEntity<SupportMessageResponse> createMessageTicket(Authentication authentication,
                                                                      SupportMessageCreateRequest request,
                                                                      @PathVariable Long id){
        SupportMessageResponse message = supportMessageService.createSupportMessage(authentication, request, id);
        return ResponseEntity.ok(message);
    }

}
