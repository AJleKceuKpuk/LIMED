package com.limed_backend.security.service;

import com.limed_backend.security.entity.Sanction;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.SanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionCacheService {

    private final SanctionRepository sanctionRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "sanctionCache", key = "#user.id")
    public List<Sanction> findAllSanctionsUser(User user, String type) {
        return sanctionRepository.findActiveSanctions(user, type);
    }

    public void deleteSanctionCache(User user){
        Cache sanctionCache = cacheManager.getCache("sanctionCache");
        sanctionCache.evict(user.getId());;
    }
}
