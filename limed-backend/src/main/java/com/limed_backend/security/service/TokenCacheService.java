package com.limed_backend.security.service;

import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final TokenRepository tokenRepository;

    /** Поиск и добавление токена в кэш*/
    @Cacheable(value = "tokenCache", key = "#jti", unless = "#result == null")
    public Token getTokenByJti(String jti) {
        return tokenRepository.findByJti(jti);
    }

    /** Удаление токена из кэша*/
    @CacheEvict(value = "tokenCache", key = "#jti")
    public void revokeToken(String jti) {
        Token token = getTokenByJti(jti);
        if (token != null && !token.getRevoked()) {
            token.setRevoked(true);
            tokenRepository.save(token);
        }
    }
}
