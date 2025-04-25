package com.limed_backend.security.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Data
public class JwtCore {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // 15 минут для access token (в миллисекундах)
    @Value("${app.jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    // 7 дней для refresh token (в миллисекундах)
    @Value("${app.jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    //генератор ключа подписи
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Генерация access token
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        return Jwts.builder()
                .setSubject(username)                                                   //добавляем имя в access token
                .setId(UUID.randomUUID().toString())                                    //создаем уникальный индификатор jti
                .setIssuedAt(now)                                                       //добавляем начало действия
                .setExpiration(expiryDate)                                              //добавляем конец действия
                .claim("tokenType", "access")                              //добавляем тип токена
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)                    //подписываем с помощью ключа подписи
                .compact();
    }

    // Генерация refresh token
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        return Jwts.builder()
                .setSubject(username)                                               //добавляем имя в refresh token
                .setId(UUID.randomUUID().toString())                                //создаем уникальный индификатор jti
                .setIssuedAt(now)                                                   //добавляем начало действия
                .setExpiration(expiryDate)                                          //добавляем конец действия
                .claim("tokenType", "refresh")                         //добавляем тип токена
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)                //подписываем с помощью ключа подписи
                .compact();
    }

    //Получение Jwt токен из запроса Header
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Извлечение claims из токена
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            // Если токен истёк, но подпись корректна
            return ex.getClaims();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("Неверный токен");
        }
    }

    // Получение Jti из Claim
    public String getJti(String token) {
        return getClaims(token).getId();
    }

    // Получение времени окончания токена
    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    // Получение имени пользователя
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // Метод для извлечения типа токена (access/refresh) из JWT
    public String getTokenTypeFromToken(String token) {
        return getClaims(token).get("tokenType", String.class);
    }


    // проверка токена на валидность и соответствия типу
    public boolean validateToken(String token, String typeToken) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException((typeToken != null ? typeToken : "Token") + " not found");
        }
        if (typeToken != null && !typeToken.trim().isEmpty()) {
            String tokenType = getTokenTypeFromToken(token);
            if (!typeToken.equals(tokenType)) {
                throw new JwtException("Provided token does not have the expected type: expected "
                        + typeToken + " but found " + tokenType);
            }
        }
        try {
            getClaims(token);
        } catch (Exception ex) {
            throw new JwtException("Invalid or expired " + (typeToken != null ? typeToken : "token")
                    + ": " + ex.getMessage(), ex);
        }
        return true;
    }
}
