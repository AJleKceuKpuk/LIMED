package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.GiveBlockRequest;
import com.limed_backend.security.dto.Requests.UnblockRequest;
import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.BlockingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockingService {

    private final BlockingRepository blockingRepository;
    private final UserService userService;
    private final CacheManager cacheManager;
    private final UserCacheService userCache;

    //поиск всех блокировок пользователя
    @Cacheable(value = "blockingCache", key = "#user.id")
    public List<Blocking> findAllBlocksUser(User user, String type) {
        return blockingRepository.findActiveBlockings(user, type);
    }

    // выдать блокировку пользователя
    public String giveBlock(GiveBlockRequest request, Authentication authentication) {
        User admin = userCache.findUserByUsername(authentication.getName());
        User user = userCache.findUserByUsername(request.getUsername());

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = DurationParser.parseDuration(request.getDuration());
        LocalDateTime endTime = startTime.plus(duration);

        Blocking block = Blocking.builder()
                .blockingType(request.getBlockingType())
                .startTime(startTime)
                .endTime(endTime)
                .reason(request.getReason())
                .user(user)
                .blockedBy(admin)
                .build();
        blockingRepository.save(block);
        return "Пользователь " + user.getUsername() + " заблокирован до " + endTime;
    }

    // снять блокировку пользователя
    @Transactional
    public String unblock(UnblockRequest request, Authentication authentication) {
        User user = userCache.findUserByUsername(request.getUsername());
        User admin = userCache.findUserByUsername(authentication.getName());
        List<Blocking> activeBlocks = blockingRepository.findActiveBlockings(user, request.getBlockingType());

        Cache cache = cacheManager.getCache("blockingCache");

        if (activeBlocks.isEmpty()) {
            return "Нет активных блокировок типа " + request.getBlockingType() + " для пользователя " + user.getUsername();
        }

        activeBlocks.forEach(block -> {
            block.setRevokedBlock(true);
            block.setRevokedBy(admin);
            if (cache != null) {
                cache.evict(block.getId());
            }
        });
        blockingRepository.saveAll(activeBlocks);

        return "Пользователь " + user.getUsername() + " разблокирован для типа " + request.getBlockingType();
    }
}
