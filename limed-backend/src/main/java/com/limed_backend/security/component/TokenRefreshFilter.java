package com.limed_backend.security.component;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TokenRefreshFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtCore jwtCore;

    @Autowired
    private UserRepository userRepository;

    // Порог времени для обновления токена (15 минут)
    private static final long REFRESH_THRESHOLD_MINUTES = 1;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Получаем токен из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        if (accessToken != null) {
            try {
                // Валидируем access token. Если срок жизни токена истёк, tokenService.validateToken
                // должен вернуть false.
                if (jwtCore.validateToken(accessToken)) {
                    // Извлекаем имя пользователя из токена.
                    String username = jwtCore.getUsernameFromToken(accessToken);
                    Optional<User> optionalUser = userRepository.findByUsername(username);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        // Если поле lastTokenRefresh уже установлено, вычисляем, сколько минут прошло.
                        if (user.getLastTokenRefresh() != null) {
                            long minutesSinceRefresh = Duration.between(user.getLastTokenRefresh(), LocalDateTime.now()).toMinutes();
                            if (minutesSinceRefresh >= REFRESH_THRESHOLD_MINUTES) {
                                // Пользователь активен, поэтому выдаём новый access token.
                                String newAccessToken = tokenService.issueAccessToken(username);
                                // Обновляем время последнего обновления токена, помечая пользователя активным.
                                user.updateTokenRefresh();
                                userRepository.save(user);
                                // Добавляем новый токен в заголовок ответа.
                                response.setHeader("New-Access-Token", newAccessToken);
                            }
                        } else {
                            // Если lastTokenRefresh по какой-то причине не установлен, обновляем его.
                            String newAccessToken = tokenService.issueAccessToken(username);
                            user.updateTokenRefresh();
                            userRepository.save(user);
                            response.setHeader("New-Access-Token", newAccessToken);
                        }
                    }
                }
            } catch (Exception e) {
                // Здесь можно добавить логирование ошибки. Вы также можете выставить статус ответа,
                // если требуется специальная обработка ошибок.
                e.printStackTrace();
            }
        }

        // Продолжаем выполнение фильтр-цепочки
        filterChain.doFilter(request, response);
    }
}
