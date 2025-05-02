package com.limed_backend.security.service;

import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.BlockingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockingService {

    private final BlockingRepository blockingRepository;

    @Cacheable(value = "blockingCache", key = "#user.id")
    public List<Blocking> allBlockings(User user, String type) {
        return blockingRepository.findActiveBlockings(user, type);
    }
}
