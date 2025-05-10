package com.limed_backend.security.service;

import com.limed_backend.security.dto.Sanction.CreateSanctionRequest;
import com.limed_backend.security.dto.Sanction.DeleteSanctionRequest;
import com.limed_backend.security.dto.Sanction.ActiveSanctionResponse;
import com.limed_backend.security.dto.Sanction.InactiveSanctionResponse;
import com.limed_backend.security.entity.Sanction;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.mapper.SanctionsMapper;
import com.limed_backend.security.repository.SanctionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanctionService {

    private final SanctionRepository sanctionRepository;
    private final UserCacheService userCache;
    private final SanctionCacheService sanctionCache;
    private final SanctionsMapper sanctionsMapper;

    public List<ActiveSanctionResponse> getAllActiveSanctions(int page){
        Page<Sanction> sanctions = sanctionCache.findAllActiveSanctions(page);
        return sanctions.stream()
                .map(sanctionsMapper::toActiveSanctionResponse)
                .toList();
    }

    public List<InactiveSanctionResponse> getAllInactiveSanctions(int page){
        Page<Sanction> sanctions = sanctionCache.findAllActiveSanctions(page);
        return sanctions.stream()
                .map(sanctionsMapper::toInactiveSanctionResponse)
                .toList();
    }

    // выдать блокировку пользователю
    @Transactional
    public String giveSanction(CreateSanctionRequest request, Authentication authentication) {
        User admin = userCache.findUserByUsername(authentication.getName());
        User user = userCache.findUserByUsername(request.getUsername());

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = DurationParser.parseDuration(request.getDuration());
        LocalDateTime endTime = startTime.plus(duration);

        Sanction sanction = Sanction.builder()
                .sanctionType(request.getSanctionType())
                .startTime(startTime)
                .endTime(endTime)
                .reason(request.getReason())
                .user(user)
                .sanctionedBy(admin)
                .build();
        sanctionRepository.save(sanction);
        sanctionCache.addSanctionToCache(user, sanction);
        return "Пользователь " + user.getUsername() + " заблокирован до " + endTime;
    }

    // снять блокировку пользователя
    @Transactional
    public String unsanctioned(DeleteSanctionRequest request, Authentication authentication) {
        User user = userCache.findUserByUsername(request.getUsername());
        User admin = userCache.findUserByUsername(authentication.getName());
        List<Sanction> activeSanctions = sanctionRepository.findActiveSanctions(user, request.getSanctionType());

        if (activeSanctions.isEmpty()) {
            return "Нет активных блокировок типа " + request.getSanctionType() + " для пользователя " + user.getUsername();
        }

        activeSanctions.forEach(sanction -> {
            sanction.setRevokedSanction(true);
            sanction.setRevokedBy(admin);
            sanctionCache.removeSanctionFromCache(user, sanction);
        });
        sanctionRepository.saveAll(activeSanctions);
        return "Пользователь " + user.getUsername() + " разблокирован для типа " + request.getSanctionType();
    }
}
