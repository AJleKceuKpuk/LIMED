package com.limed_backend.security.service;

import com.limed_backend.security.dto.Support.SupportCreateRequest;
import com.limed_backend.security.dto.Support.SupportStatusRequest;
import com.limed_backend.security.dto.Support.SupportResponse;
import com.limed_backend.security.entity.Support;
import com.limed_backend.security.entity.SupportMessage;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.entity.enums.SupportStatus;
import com.limed_backend.security.entity.enums.SupportType;
import com.limed_backend.security.exception.exceprions.*;
import com.limed_backend.security.mapper.SupportMapper;
import com.limed_backend.security.repository.SupportMessageRepository;
import com.limed_backend.security.repository.SupportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRepository supportRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserCacheService userCache;
    private final UserService userService;
    private final SupportMapper supportMapper;


    public Support findSupportById(Long id){
        return supportRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean hasUnreadSupportMessages(Authentication authentication) {
        User user = userCache.findUserByUsername(authentication.getName());
        return supportRepository.existsByUserAndReadByUserFalse(user);
    }

    public boolean checkAdminAccess(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        return userService.isAdmin(user);
    }

    public boolean checkUserAccess(Authentication authentication, Support support){
        User user = userCache.findUserByUsername(authentication.getName());
        return support.getUser().equals(user);
    }

    public SupportResponse getSupportById(Authentication authentication, Long id){
        Support support = findSupportById(id);
        if (!checkAdminAccess(authentication) && !checkUserAccess(authentication, support)){
            throw new AdminAccessRequiredException();
        }
        return supportMapper.toSupportResponse(support);
    }


    //список всех обращений для админа со статусом
    public Page<SupportResponse> getAllSupportByAdmin(Authentication authentication,
                                                      SupportStatusRequest request,
                                                      int size,
                                                      int page) {

        if (!checkAdminAccess(authentication)){
            throw new AdminAccessRequiredException();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Support> supports;
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            supports = supportRepository.findAll(pageable);
        } else {
            SupportStatus status = SupportStatus.valueOf(request.getStatus());
            supports = supportRepository.getAllSupportByStatus(status, pageable);
        }
        return supports.map(supportMapper::toSupportResponse);
    }

    public Page<SupportResponse> getAllSupportByUser(Authentication authentication,
                                                     SupportStatusRequest request,
                                                     int size,
                                                     int page){
        User user = userCache.findUserByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Support> supports;
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            supports = supportRepository.getAllSupportByUserId(user.getId(), pageable);
        } else {
            SupportStatus status = SupportStatus.valueOf(request.getStatus());
            supports = supportRepository.getAllSupportByUserIdAndStatus(user.getId(), status, pageable);
        }
        return supports.map(supportMapper::toSupportResponse);
    }

    @Transactional
    public SupportResponse createSupport(Authentication authentication, SupportCreateRequest request){
        User user = userCache.findUserByUsername(authentication.getName());
        SupportType type = SupportType.valueOf(request.getType());

        Support support = Support.builder()
                .heading(request.getHeading())
                .type(type)
                .createdAt(LocalDateTime.now())
                .status(SupportStatus.NEW)
                .user(user)
                .readByUser(true)
                .readByAdmin(false)
                .build();

        SupportMessage message = SupportMessage.builder()
                .support(support)
                .content(request.getMessage())
                .sendTime(LocalDateTime.now())
                .metadata(request.getMetadata())
                .sender(user)
                .build();

        support.setUpdatedAt(support.getCreatedAt());
        supportRepository.save(support);
        supportMessageRepository.save(message);

        return supportMapper.toSupportResponse(support);
    }

    public SupportResponse closeSupportTicket(Authentication authentication, Long id){
        if (!checkAdminAccess(authentication)){
            throw new AdminAccessRequiredException();
        }
        Support support = findSupportById(id);
        support.setStatus(SupportStatus.CLOSED);
        support.setReadByUser(false);
        supportRepository.save(support);
        return supportMapper.toSupportResponse(support);
    }

    @Transactional
    public SupportResponse reopenSupportTicket(Authentication authentication, Long id) {
        if (!checkAdminAccess(authentication)){
            throw new AdminAccessRequiredException();
        }
        Support support = findSupportById(id);
        if (support.getStatus() != SupportStatus.CLOSED) {
            throw new TicketNotClosedException();
        }
        support.setStatus(SupportStatus.OPEN);
        support.setUpdatedAt(LocalDateTime.now());
        support.setReadByUser(false);
        supportRepository.save(support);

        return supportMapper.toSupportResponse(support);
    }

    @Transactional
    public SupportResponse escalateSupportTicket(Authentication authentication, Long id) {
        if (!checkAdminAccess(authentication)){
            throw new AdminAccessRequiredException();
        }
        Support support = findSupportById(id);
        if (support.getStatus() == SupportStatus.ESCALATED) {
            throw new TicketIsEscalatedException();
        }
        support.setStatus(SupportStatus.ESCALATED);
        support.setUpdatedAt(LocalDateTime.now());
        support.setReadByUser(false);
        supportRepository.save(support);

        return supportMapper.toSupportResponse(support);
    }
}
