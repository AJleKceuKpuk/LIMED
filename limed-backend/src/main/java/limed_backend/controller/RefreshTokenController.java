package limed_backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import limed_backend.component.JwtUtil;
import limed_backend.component.TokenResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class RefreshTokenController {

    private final JwtUtil jwtUtil;

    public RefreshTokenController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

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
        newRefreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiration() / 1000));
        response.addCookie(newRefreshTokenCookie);

        // Возвращаем новый access token клиенту в теле ответа
        return new TokenResponse(newAccessToken);
    }
}

