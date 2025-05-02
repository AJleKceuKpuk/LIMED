package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.LoginRequest;
import com.limed_backend.security.dto.Requests.RegistrationRequest;
import com.limed_backend.security.dto.Responses.TokenResponse;
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


    // /registration
    public String registration(RegistrationRequest request) {
        userService.validateUsernameAvailability(request.getUsername());
        userService.validateEmailAvailability(request.getEmail());
        userService.createAndSaveUser(request);
        return "Пользователь зарегистрирован";
    }

    // Логика /login
    public TokenResponse login(HttpServletRequest request, LoginRequest loginRequest, HttpServletResponse response) {
        try {
            authenticateUser(loginRequest);
        } catch (Exception ex) {
            throw new InvalidUsernameOrPasswordException();
        }
        User user = userService.findUserByUsername(loginRequest.getUsername());
        userService.updateUserStatus(user.getId(), "online");
        return tokenService.generateAndSetTokens(request, user, response);
    }

    // Проверка пользователя
    public void authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
    }


    // Логика /logout
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.revokeRefreshTokenFromCookies(request, response);
        String accessToken = jwtCore.getJwtFromHeader(request);
        if (accessToken == null){
            return ResponseEntity.ok("Без авторизации нельзя");
        }
        User user = userService.findUserByUsername(jwtCore.getUsernameFromToken(accessToken));
        tokenService.revokeToken(jwtCore.getJti(accessToken));
        userService.updateUserStatus(user.getId(), "offline");
        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }

}

