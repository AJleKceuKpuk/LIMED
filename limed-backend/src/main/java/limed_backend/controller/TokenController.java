package limed_backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import limed_backend.dto.TokenResponse;
import limed_backend.jwt.JwtUtil;
import limed_backend.models.TokenRecord;
import limed_backend.repository.TokenRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final JwtUtil jwtUtil;
    private final TokenRecordRepository tokenRecordRepository;

    public TokenController(JwtUtil jwtUtil, TokenRecordRepository tokenRecordRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenRecordRepository = tokenRecordRepository;
    }

    /**
     * Эндпоинт для обновления токенов (refresh token)
     */
    @PostMapping("/refresh-token")
    public TokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // Извлекаем refresh token из cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Если refresh token отсутствует или недействителен — отклоняем запрос
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token отсутствует или недействителен");
        }

        // Получаем имя пользователя из refresh token
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // Генерируем новые токены
        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        // Записываем новый refresh token в cookie
        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(true);
        newRefreshTokenCookie.setPath("/");
        // Время жизни куки рассчитывается исходя из времени истечения нового refresh token
        newRefreshTokenCookie.setMaxAge((int) ((jwtUtil.getExpirationFromToken(newRefreshToken).getTime() - System.currentTimeMillis()) / 1000));
        response.addCookie(newRefreshTokenCookie);

        // Возвращаем новый access token клиенту в теле ответа
        return new TokenResponse(newAccessToken);
    }

    /**
     * Эндпоинт для получения списка активных (не отозванных и не истёкших) токенов.
     * Этот эндпоинт пригоден для аудита и административных целей.
     */
    @GetMapping("/active")
    public List<TokenRecord> getActiveTokens() {
        return tokenRecordRepository.findByRevokedFalseAndExpirationAfter(new Date());
    }
}
