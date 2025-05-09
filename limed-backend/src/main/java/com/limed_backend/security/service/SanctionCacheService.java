package com.limed_backend.security.service;

import com.limed_backend.security.entity.Sanction;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.SanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionCacheService {

    private final SanctionRepository sanctionRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "sanctions", key = "#user.id")
    public List<Sanction> findAllSanctionsUser(User user, String type) {
        return sanctionRepository.findActiveSanctions(user, type);
    }

    @Cacheable(value = "sanctions-active", key = "#page")
    public Page<Sanction> findAllActiveSanctions(int page){
        Pageable pageable = PageRequest.of(page, 20);
        return sanctionRepository.findActiveSanctions(pageable);
    }

    @Cacheable(value = "sanctions-inactive", key = "#page")
    public Page<Sanction> findAllInactiveSanctions(int page) {
        // Создаём Pageable с номером страницы и размером страницы в 20 записей
        Pageable pageable = PageRequest.of(page, 20);
        return sanctionRepository.findInactiveSanctions(pageable);
    }

    //метод удаления запрета из кэша
    @CacheEvict(value = {"sanctions-active", "sanctions-inactive"}, allEntries = true)
    public void removeSanctionFromCache(User user, Sanction sanction) {
        Cache cache = cacheManager.getCache("sanctions");
        List<Sanction> sanctions = getListSanctionFromCache(cache, user);
        if (sanctions.isEmpty()) {
            findAllSanctionsUser(user, sanction.getSanctionType());
        };
        sanctions.removeIf(c -> c.getId().equals(sanction.getId()));
        cache.put(user.getId(), sanctions);
    }

    //метод добавления запрета в кэш
    @CacheEvict(value = {"sanctions-active", "sanctions-inactive"}, allEntries = true)
    public void addSanctionToCache(User user, Sanction sanction) {
        Cache cache = cacheManager.getCache("sanctions");
        List<Sanction> sanctions = getListSanctionFromCache(cache, user);
        if (sanctions.isEmpty()) {
            findAllSanctionsUser(user, sanction.getSanctionType());
        };

        if (sanctions.stream().noneMatch(c -> c.getId().equals(sanction.getId()))) {
            sanctions.add(sanction);
        }
        cache.put(user.getId(), sanctions);
    }

    public List<Sanction> getListSanctionFromCache(Cache cache, User user) {
        List<?> rawList = cache.get(user.getId(), List.class);
        List<Sanction> listSanctions = new ArrayList<>();
        if (rawList != null) {
            for (Object item : rawList) {
                if (item instanceof Sanction) {
                    listSanctions.add((Sanction) item);
                }
            }
        }
        return listSanctions;
    }
}
