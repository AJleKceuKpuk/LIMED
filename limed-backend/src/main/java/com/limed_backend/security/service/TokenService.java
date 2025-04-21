package com.limed_backend.security.service;

import com.limed_backend.security.config.JwtCore;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

    //запись Refresh токена в базу данных
    public String issueRefreshToken(String username) {
        String token = jwtCore.generateRefreshToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "refresh");
        tokenRepository.save(record);
        return token;
    }

    //Отзыв токена (revoke из БД)
    public void revokeToken(String jti) {
        Token token = tokenRepository.findByJti(jti);
        if (token != null && !token.getRevoked()) {
            token.setRevoked(true); // При установке true, метод setRevoked() сам выставит revokedAt
            tokenRepository.save(token);
        }
    }

    //Отзыв все токенов пользователя и БД
    public void revokeAllTokens(String username) {
        List<Token> tokens = tokenRepository.findByUsernameAndRevokedFalse(username);
        for (Token token : tokens) {
            token.setRevoked(true);
        }
        tokenRepository.saveAll(tokens);
    }


    /* ==================================================================== */
    // Логика обновления Access токена
    public String refreshAccessToken(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookies(request);
        jwtCore.validateToken(refreshToken, "refresh");

        String accessToken = jwtCore.getJwtFromHeader(request);
        Claims accessClaims = jwtCore.getClaims(accessToken);

        String usernameFromAccess = accessClaims.getSubject();

        validateAccessTokenRecord(accessClaims);
        validateTokenOwnership(refreshToken, accessClaims);

        revokeToken(accessClaims.getId());
        return issueAccessToken(usernameFromAccess);
    }

    // Извлекает Refresh Токен из Куки
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Проверяет, что access токен существует в базе и не отозван
    private void validateAccessTokenRecord(Claims accessClaims) {
        String accessJti = accessClaims.getId();
        Token accessTokenRecord = tokenRepository.findByJti(accessJti);
        if (accessTokenRecord == null || accessTokenRecord.getRevoked()) {
            throw new JwtException("Access токен отозван или не найден");
        }
    }

    // Сверяет, что refresh и access токены принадлежат одному пользователю
    private void validateTokenOwnership(String refreshToken, Claims accessClaims) {
        String usernameFromAccess = accessClaims.getSubject();
        String usernameFromRefresh = jwtCore.getUsernameFromToken(refreshToken);
        if (!usernameFromAccess.equals(usernameFromRefresh)) {
            throw new JwtException("Токены принадлежат разным пользователям");
        }
    }

    /* ==================================================================== */


    //ежедневная проверка токенов для удаления старых, которые истекли (в 8:00)
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Date now = new Date();
        tokenRepository.deleteAllByExpirationBefore(now);
    }

}