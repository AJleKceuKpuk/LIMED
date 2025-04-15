package com.limed_backend.security.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Data
public class JwtCore {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Пример: 15 минут для access token (в миллисекундах)
    @Value("${app.jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    // Пример: 7 дней для refresh token (в миллисекундах)
    @Value("${app.jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Генерация токена доступа (access token)
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        return Jwts.builder()
                .setSubject(username)
                // Уникальный идентификатор токена (jti)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Добавляем тип токена в claims для явного указания, что это access token
                .claim("tokenType", "access")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Генерация refresh token с уникальным идентификатором (jti)
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Добавляем тип токена в claims для явного указания, что это refresh token
                .claim("tokenType", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Извлечение claims из токена через общий метод
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // Метод для извлечения типа токена (access/refresh) из JWT (если потребуется)
    public String getTokenTypeFromToken(String token) {
        return getClaims(token).get("tokenType", String.class);
    }

    public boolean validateToken(String token) {
        try {
            // Если токен успешно распарсен – он считается валидным с точки зрения подписи и срока действия.
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Здесь можно реализовать логирование ошибки для подробного анализа.
            return false;
        }
    }
}
