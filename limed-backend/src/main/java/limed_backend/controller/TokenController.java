package limed_backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import limed_backend.dto.TokenResponse;
import limed_backend.jwt.JwtUtil;
import limed_backend.models.Token;
import limed_backend.repository.TokenRepository;
import limed_backend.services.TokenService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;

    public TokenController(JwtUtil jwtUtil, TokenRepository tokenRepository, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
    }

    /**
     * Эндпоинт для обновления токенов (refresh token).
     * Refresh token извлекается из cookies, затем проверяется его валидность,
     * сверяется запись из базы данных (не отозвана, не истекла) и генерируются новые токены.
     * Новый refresh token отправляется обратно в виде HttpOnly cookie,
     * а access token возвращается в теле ответа.
     */
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // Извлекаем refresh token из cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Проверяем наличие и базовую валидность refresh token'а (подпись, срок действия)
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token отсутствует или недействителен");
        }

        // Извлекаем уникальный идентификатор (jti) и по нему получаем запись токена из базы
        String jti = jwtUtil.getJti(refreshToken);
        Token persistedToken = tokenRepository.findByJti(jti);
        if (persistedToken == null || persistedToken.getRevoked() || persistedToken.getExpiration().before(new Date())) {
            throw new RuntimeException("Refresh token не найден, отозван или истёк");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // Опционально: можно отозвать использованный refresh token, если реализуете механизм ротации
        // tokenService.revokeToken(jti);

        // Генерация новых токенов с сохранением записей в базу данных
        String newAccessToken = tokenService.issueAccessToken(username);
        String newRefreshToken = tokenService.issueRefreshToken(username);

        // Обновляем refresh token в cookies
        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(true);
        newRefreshTokenCookie.setPath("/");
        // Устанавливаем время жизни куки в секундах, исходя из срока действия нового refresh token
        int maxAge = (int) ((jwtUtil.getExpirationFromToken(newRefreshToken).getTime() - System.currentTimeMillis()) / 1000);
        newRefreshTokenCookie.setMaxAge(maxAge);
        response.addCookie(newRefreshTokenCookie);

        // Возвращаем новый access token клиенту в JSON-ответе
        return new TokenResponse(newAccessToken);
    }

    /**
     * Эндпоинт для получения списка активных (не отозванных и не истёкших) токенов.
     * Подходит для аудита и административных целей.
     */
    @GetMapping("/active")
    public List<Token> getActiveTokens() {
        return tokenRepository.findByRevokedFalseAndExpirationAfter(new Date());
    }
}
