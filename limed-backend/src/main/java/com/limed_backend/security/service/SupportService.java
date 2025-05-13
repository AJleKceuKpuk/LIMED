package com.limed_backend.security.service;

import com.limed_backend.security.dto.Support.SupportCreateRequest;
import com.limed_backend.security.dto.Support.SupportStatusRequest;
import com.limed_backend.security.dto.Support.SupportResponse;
import com.limed_backend.security.entity.Support;
import com.limed_backend.security.entity.SupportMessage;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.entity.enums.SupportStatus;
import com.limed_backend.security.entity.enums.SupportType;
import com.limed_backend.security.exception.exceprions.ResourceNotFoundException;
import com.limed_backend.security.exception.exceprions.AdminAccessRequiredException;
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

    public Support getSupportById(Long id){
        return supportRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    public List<Support> getAllSupportUser(Authentication authentication){
        User user = userCache.findUserByUsername(authentication.getName());
        return supportRepository.getAllSupportByUserId(user.getId())
                .orElseGet(ArrayList::new);
    }

    public Page<Support> getAllSupport(Authentication authentication, SupportStatusRequest request, int size, int page) {
        User user = userCache.findUserByUsername(authentication.getName());
        if (!userService.isAdmin(user)) {
            throw new AdminAccessRequiredException();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            return supportRepository.findAll(pageable);
        }
        SupportStatus status = SupportStatus.valueOf(request.getStatus());
        return supportRepository.getAllSupportByStatus(status, pageable);
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
        User admin = userCache.findUserByUsername(authentication.getName());
        if (!userService.isAdmin(admin)){
            throw new AdminAccessRequiredException();
        }

        Support support = getSupportById(id);
        support.setStatus(SupportStatus.CLOSED);
        supportRepository.save(support);
        return supportMapper.toSupportResponse(support);
    }

    @Transactional
    public SupportResponse reopenSupportTicket(Authentication authentication, Long id) {
        User admin = userCache.findUserByUsername(authentication.getName());
        if (!userService.isAdmin(admin)) {
            throw new AdminAccessRequiredException();
        }
        Support support = getSupportById(id);
        if (support.getStatus() != SupportStatus.CLOSED) {
            //TODO Сделать норм эксепшн
            throw new IllegalStateException("Можно повторно открыть только закрытые обращения");
        }

        support.setStatus(SupportStatus.OPEN);
        support.setUpdatedAt(LocalDateTime.now());
        supportRepository.save(support);

        return supportMapper.toSupportResponse(support);
    }

    @Transactional
    public SupportResponse escalateSupportTicket(Authentication authentication, Long id) {
        User admin = userCache.findUserByUsername(authentication.getName());
        if (!userService.isAdmin(admin)) {
            throw new AdminAccessRequiredException();
        }
        Support support = getSupportById(id);

        if (support.getStatus() == SupportStatus.ESCALATED) {
            //TODO Сделать норм эксепшн
            throw new IllegalStateException("Обращение уже эскалировано");
        }

        support.setStatus(SupportStatus.ESCALATED);
        support.setUpdatedAt(LocalDateTime.now());
        supportRepository.save(support);

        return supportMapper.toSupportResponse(support);
    }
}
