package com.limed_backend.security.service;

import com.limed_backend.security.dto.Auth.LoginRequest;
import com.limed_backend.security.dto.Auth.RegistrationRequest;
import com.limed_backend.security.dto.Token.TokenResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.*;
import com.limed_backend.security.config.JwtCore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtCore jwtCore;
    private final TokenService tokenService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserCacheService userCache;
    private final TokenCacheService tokenCache;


    // регистрация пользователей
    public String registration(RegistrationRequest request) {
        userService.validateUsernameAvailability(request.getUsername());
        userService.validateEmailAvailability(request.getEmail());
        userService.createAndSaveUser(request);
        return "Пользователь зарегистрирован";
    }

    // вход пользователя и его проверка
    public TokenResponse login(HttpServletRequest request, LoginRequest loginRequest, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (Exception ex) {
            throw new InvalidUsernameOrPasswordException();
        }
        User user = userCache.findUserByUsername(loginRequest.getUsername());
        userService.updateUserStatus(user.getId(), "online");
        return tokenService.generateAndSetTokens(request, user, response);
    }

    // Логика выхода
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.revokeRefreshTokenFromCookies(request, response);
        String accessToken = jwtCore.getJwtFromHeader(request);
        if (accessToken == null){
            return ResponseEntity.ok("Без авторизации нельзя");
        }
        User user = userCache.findUserByUsername(jwtCore.getUsernameFromToken(accessToken));
        tokenCache.revokeToken(jwtCore.getJti(accessToken));
        userService.updateUserStatus(user.getId(), "offline");
        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }

}

