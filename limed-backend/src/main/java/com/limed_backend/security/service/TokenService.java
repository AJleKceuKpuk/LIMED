package com.limed_backend.security.service;

import com.limed_backend.security.jwt.JwtCore;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private JwtCore jwtCore;

    @Autowired
    private TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String issueAccessToken(String username) {
        // Генерация access token с уникальным идентификатором (jti)
        String token = jwtCore.generateAccessToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        // Создаем запись токена: по умолчанию revoked == false и revokedAt == null
        Token record = new Token(jti, username, issuedAt, expiration, "access");
        tokenRepository.save(record);
        return token;
    }

    /**
     * Выдает refresh token для заданного пользователя.
     *
     * @param username Имя пользователя.
     * @return Сгенерированный refresh token.
     */
    public String issueRefreshToken(String username) {
        // Генерация refresh token с уникальным идентификатором (jti)
        String token = jwtCore.generateRefreshToken(username);
        String jti = jwtCore.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtCore.getExpirationFromToken(token);
        // Создаем запись токена: по умолчанию revoked == false и revokedAt == null
        Token record = new Token(jti, username, issuedAt, expiration, "refresh");
        tokenRepository.save(record);
        return token;
    }

    /**
     * Отзывает токен по его уникальному идентификатору (jti).
     * Если токен найден и не отозван, меняет статус revoked на true,
     * что приводит к установке времени отзыва (revokedAt) в текущий момент.
     *
     * @param jti Уникальный идентификатор токена, который нужно отозвать.
     */
    public void revokeToken(String jti) {
        Token token = tokenRepository.findByJti(jti);
        if (token != null && !token.getRevoked()) {
            token.setRevoked(true); // При установке true, метод setRevoked() сам выставит revokedAt
            tokenRepository.save(token);
        }
    }

    /**
     * Метод запускается по расписанию каждый день в 08:00.
     * Аннотация @Transactional гарантирует, что операции удаления будут
     * выполнены в транзакционном контексте.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        Date now = new Date();
        int deletedCount = tokenRepository.deleteAllByExpirationBefore(now);
        System.out.println("Deleted " + deletedCount + " old tokens.");
    }
}