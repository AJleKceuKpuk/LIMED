package com.limed_backend.security.service;

import com.limed_backend.security.dto.Support.SupportMessageCreateRequest;
import com.limed_backend.security.dto.Support.SupportMessageResponse;
import com.limed_backend.security.entity.Support;
import com.limed_backend.security.entity.SupportMessage;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.AdminAccessRequiredException;
import com.limed_backend.security.mapper.SupportMapper;
import com.limed_backend.security.repository.SupportMessageRepository;
import com.limed_backend.security.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportRepository supportRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserCacheService userCache;
    private final UserService userService;
    private final SupportMapper supportMapper;
    private final SupportService supportService;

    public void setStatusRead(User user, Support support){
        if (userService.isAdmin(user)) {
            support.setReadByAdmin(true);
        } else if (support.getUser().equals(user)) {
            support.setReadByUser(true);
        }
    }

    @Transactional
    public Page<SupportMessageResponse> getAllMessagesBySupport(Authentication authentication, Long supportId, int size, int page) {
        Support support = supportService.findSupportById(supportId);
        User user = userCache.findUserByUsername(authentication.getName());

        if (!supportService.checkAdminAccess(authentication) && !supportService.checkUserAccess(authentication, support)){
            throw new AdminAccessRequiredException();
        }

        setStatusRead(user, support);
        supportRepository.save(support);

        Pageable pageable = PageRequest.of(page, size, Sort.by("sendTime").descending());
        Page<SupportMessage> messagesPage = supportMessageRepository.findAllBySupportId(supportId, pageable);
        return messagesPage.map(supportMapper::toSupportMessageResponse);
    }

    @Transactional
    public SupportMessageResponse createSupportMessage(Authentication authentication, SupportMessageCreateRequest request, Long supportId){
        Support support = supportService.findSupportById(supportId);
        User user = userCache.findUserByUsername(authentication.getName());
        if (!supportService.checkAdminAccess(authentication) && !supportService.checkUserAccess(authentication, support)){
            throw new AdminAccessRequiredException();
        }

        SupportMessage message = SupportMessage.builder()
                .content(request.getContent())
                .sendTime(LocalDateTime.now())
                .metadata(request.getMetadata())
                .support(support)
                .sender(user)
                .build();
        supportMessageRepository.save(message);
        setStatusRead(user, support);
        return supportMapper.toSupportMessageResponse(message);
    }
}
