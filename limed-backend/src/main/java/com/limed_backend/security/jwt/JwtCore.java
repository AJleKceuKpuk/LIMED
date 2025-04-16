package com.limed_backend.security.jwt;

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
                .setId(UUID.randomUUID().toString())                                    //создаем уникальный индификатор
                .setIssuedAt(now)                                                       //добавляем начало действия
                .setExpiration(expiryDate)                                              //добавляем конец действия
                .claim("tokenType", "access")                                     //добавляем тип токена
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
                .claim("tokenType", "refresh")                                //добавляем тип токена
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)                //подписываем с помощью ключа подписи
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

    // Метод для извлечения типа токена (access/refresh) из JWT
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
