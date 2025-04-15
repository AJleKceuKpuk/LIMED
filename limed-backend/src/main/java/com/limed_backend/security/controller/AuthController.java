package com.limed_backend.security.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.component.JwtCore;
import com.limed_backend.security.dto.LoginRequest;
import com.limed_backend.security.dto.RegistrationRequest;
import com.limed_backend.security.dto.TokenResponse;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TokenService tokenService;



    public AuthController(AuthenticationManager authenticationManager, JwtCore jwtCore) {
        this.authenticationManager = authenticationManager;
        this.jwtCore = jwtCore;
    }

    @GetMapping("/start")
    public String welcome() {
        return "Добро пожаловать! Доступно всем.";
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // 1. Аутентификация по логину и паролю
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Генерация и сохранение токенов через TokenService
        String accessToken = tokenService.issueAccessToken(loginRequest.getUsername());
        String refreshToken = tokenService.issueRefreshToken(loginRequest.getUsername());

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.updateTokenRefresh(); // Обновляет lastTokenRefresh и устанавливает online = true
            userRepository.save(user);
        }

        // 3. Создание httpOnly cookie для refresh token
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);   // недоступно для JavaScript
        refreshTokenCookie.setSecure(true);       // передается только по HTTPS
        refreshTokenCookie.setPath("/");          // доступно для всего приложения

        // 4. Вычисление времени жизни куки, исходя из срока действия refresh token
        Date refreshExpiration = jwtCore.getExpirationFromToken(refreshToken);
        int maxAge = (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
        refreshTokenCookie.setMaxAge(maxAge);

        // 5. Добавление куки в ответ
        response.addCookie(refreshTokenCookie);

        // 6. Возврат access token в теле ответа
        return new TokenResponse(accessToken);
    }

    @PostMapping("/registration")
    public String register(@RequestBody RegistrationRequest request) {
        // Если пользователь с таким именем уже существует
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Пользователь с таким именем уже существует";
        }

        // Поиск роли "USER" в БД
        Role roleUser = roleRepository.findByName("USER");
        if (roleUser == null) {
            return "Роль USER не найдена. Обратитесь к администратору.";
        }

        // Регистрация нового пользователя с ролью USER
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(roleUser))
                .online(false)             // Пользователь по умолчанию офлайн
                .lastTokenRefresh(null)    // Вход в систему не произведён, поэтому времени нет
                .build();
        userRepository.save(user);
        return "Пользователь зарегистрирован";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Обработка refresh token из cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    try {
                        String refreshToken = cookie.getValue();
                        String refreshJti = jwtCore.getJti(refreshToken);
                        // Отзыв refresh token через TokenService
                        tokenService.revokeToken(refreshJti);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // Очистка куки
                    cookie.setValue(null);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        // Обработка access token из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String accessJti = jwtCore.getJti(accessToken);
                tokenService.revokeToken(accessJti);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }
}
