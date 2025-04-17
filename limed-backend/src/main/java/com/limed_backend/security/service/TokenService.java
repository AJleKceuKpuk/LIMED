package com.limed_backend.security.service;

import com.limed_backend.security.jwt.JwtCore;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Autowired
    private JwtCore jwtCore;
    @Autowired
    private TokenRepository tokenRepository;

    //запись Access токена в базу данных
    public String issueAccessToken(String username) {
        String token = jwtCore.generateAccessToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "access");
        tokenRepository.save(record);
        return token;
    }

    //запись Refresh токена
    public String issueRefreshToken(String username) {
        String token = jwtCore.generateRefreshToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "refresh");
        tokenRepository.save(record);
        return token;
    }

    //Отзыв токена
    public void revokeToken(String jti) {
        Token token = tokenRepository.findByJti(jti);
        if (token != null && !token.getRevoked()) {
            token.setRevoked(true); // При установке true, метод setRevoked() сам выставит revokedAt
            tokenRepository.save(token);
        }
    }

    public void revokeAllTokens(String username) {
        List<Token> tokens = tokenRepository.findByUsernameAndRevokedFalse(username);
        for (Token token : tokens) {
            token.setRevoked(true);
        }
        tokenRepository.saveAll(tokens);
    }

    //ежедневная проверка токенов для удаления старых, которые истекли (в 8:00)
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Date now = new Date();
        int deletedCount = tokenRepository.deleteAllByExpirationBefore(now);
    }
}