package com.limed_backend.security.service;

import com.limed_backend.security.config.JwtCore;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private final JwtCore jwtCore;
    private final TokenRepository tokenRepository;

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

    //запись Access токена в базу данных
    public String issueAccessToken(HttpServletRequest request, String username) {
        String token = jwtCore.generateAccessToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "access", request.getRemoteAddr());
        tokenRepository.save(record);
        return token;
    }

    //запись Refresh токена в базу данных
    public String issueRefreshToken(HttpServletRequest request, String username) {
        String token = jwtCore.generateRefreshToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        Token record = new Token(jti, username, issuedAt, expiration, "refresh", request.getRemoteAddr());
        tokenRepository.save(record);
        return token;
    }

    // создание и выдача токенов в куки и в тело ответа
    public TokenResponse generateAndSetTokens(HttpServletRequest request, User user, HttpServletResponse response) {
        String newAccessToken = issueAccessToken(request, user.getUsername());
        String newRefreshToken = issueRefreshToken(request, user.getUsername());
        addRefreshTokenCookie(newRefreshToken, response);
        return new TokenResponse(newAccessToken);
    }

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
        return issueAccessToken(request, usernameFromAccess);
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

    //Отзыв токена (revoke из БД)
    public void revokeToken(String jti) {
        Token token = tokenRepository.findByJti(jti);
        if (token != null && !token.getRevoked()) {
            token.setRevoked(true); // При установке true, метод setRevoked() сам выставит revokedAt
            tokenRepository.save(token);
        }
    }

    //Отзыв всех токенов пользователя и БД
    public void revokeAllTokens(String username) {
        List<Token> tokens = tokenRepository.findByUsernameAndRevokedFalse(username);
        for (Token token : tokens) {
            token.setRevoked(true);
        }
        tokenRepository.saveAll(tokens);
    }

    // Отзыв Refresh токена и очистка куки на клиенте
    public void revokeRefreshTokenFromCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.debug("Cookie null");
            return;
        }
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                try {
                    String refreshToken = cookie.getValue();
                    String refreshJti = jwtCore.getJti(refreshToken);
                    revokeToken(refreshJti);
                } catch (Exception ex) {
                    logger.error("Ошибка при отзыве токена", ex);
                }
                clearCookie(cookie, response);
            }
        }
    }

    // Добавление RefreshToken в куки
    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(cookieLifetime(refreshToken));
        response.addCookie(refreshTokenCookie);
    }

    //под-метод для удаления куки
    private void clearCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setValue(null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // Расчет времени жизни куки Refresh токена
    private int cookieLifetime(String refreshToken) {
        Date refreshExpiration = jwtCore.getExpirationFromToken(refreshToken);
        return (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }


    // При первом вызове, если записи с данным jti нет в кэше, она будет загружена из репозитория.
    // Затем результат кэшируется в "tokenCache". Если tokenRepository вернёт null, кэширование не происходит.
    @Cacheable(value = "tokenCache", key = "#jti", unless = "#result == null")
    public Token getTokenByJti(String jti) {
        return tokenRepository.findByJti(jti);
    }



    //ежедневная проверка токенов для удаления старых, которые истекли (в 8:00)
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Date now = new Date();
        int deletedCount = tokenRepository.deleteAllByExpirationBefore(now);
        logger.info("Deleted {} expired tokens", deletedCount);
    }

}