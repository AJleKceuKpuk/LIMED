package limed_backend.services;

import limed_backend.jwt.JwtUtil;
import limed_backend.models.Token;
import limed_backend.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Выдает access token для заданного пользователя.
     *
     * @param username Имя пользователя.
     * @return Сгенерированный access token.
     */
    public String issueAccessToken(String username) {
        // Генерация access token с уникальным идентификатором (jti)
        String token = jwtUtil.generateAccessToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
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
        String token = jwtUtil.generateRefreshToken(username);
        String jti = jwtUtil.getJti(token);
        Date issuedAt = new Date();
        Date expiration = jwtUtil.getExpirationFromToken(token);
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
}
