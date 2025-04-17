package com.limed_backend.security.service;

import com.limed_backend.security.dto.LoginRequest;
import com.limed_backend.security.dto.RegistrationRequest;
import com.limed_backend.security.dto.TokenResponse;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.jwt.JwtCore;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    /* ==================================================================== */
    // Логика /registration

    public String registration(RegistrationRequest request) {
        if (isUsernameTaken(request.getUsername())) {
            return "Пользователь с таким именем уже существует";
        }
        if (isEmailTaken(request.getEmail())) {
            return "Пользователь с таким Email уже существует";
        }
        Role roleUser = getUserRole();
        if (roleUser == null) {
            return "Роль USER не найдена. Обратитесь к администратору.";
        }

        createAndSaveUser(request, roleUser);
        return "Пользователь зарегистрирован";
    }

    private boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private Role getUserRole() {
        return roleRepository.findByName("USER");
    }

    private void createAndSaveUser(RegistrationRequest request, Role userRole) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(userRole))
                .status("offline")
                .lastActivity(null)
                .dateRegistration(LocalDate.now())
                .build();
        userRepository.save(user);
    }
    /* ==================================================================== */
    // Логика /login

    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        // 1. Аутентификация
        authenticateUser(loginRequest);

        // 2. Генерация токенов
        String accessToken = tokenService.issueAccessToken(loginRequest.getUsername());
        String refreshToken = tokenService.issueRefreshToken(loginRequest.getUsername());

        // 3. Обновление данных пользователя
        updateUserTokenRefresh(loginRequest.getUsername());

        // 4. Добавление refresh token в куки
        addRefreshTokenCookie(refreshToken, response);

        // 5. Возврат access token в теле ответа
        return new TokenResponse(accessToken);
    }

    private void authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
    }

    private void updateUserTokenRefresh(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.updateTokenRefresh();
            userRepository.save(user);
        });
    }

    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(cookieLifetime(refreshToken));
        response.addCookie(refreshTokenCookie);
    }

    private int cookieLifetime(String refreshToken) {
        Date refreshExpiration = jwtCore.getExpirationFromToken(refreshToken);
        return (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    /* ==================================================================== */
    // Логика /logout

    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        revokeRefreshTokenFromCookies(request, response);
        revokeAccessTokenFromHeader(request);
        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }

    private void revokeRefreshTokenFromCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                try {
                    String refreshToken = cookie.getValue();
                    String refreshJti = jwtCore.getJti(refreshToken);
                    tokenService.revokeToken(refreshJti);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                clearCookie(cookie, response);
            }
        }
    }

    private void clearCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setValue(null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void revokeAccessTokenFromHeader(HttpServletRequest request) {
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
    }
}

