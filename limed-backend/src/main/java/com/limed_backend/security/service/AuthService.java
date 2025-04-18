package com.limed_backend.security.service;

import com.limed_backend.security.dto.LoginRequest;
import com.limed_backend.security.dto.RegistrationRequest;
import com.limed_backend.security.dto.TokenResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.jwt.JwtCore;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;

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
    private final UserService userService;

    /* ==================================================================== */
    // Логика /registration

    public String registration(RegistrationRequest request) {
        userService.validateUsername(request.getUsername());
        userService.validateEmailAvailability(request.getEmail());
        createAndSaveUser(request);
        return "Пользователь зарегистрирован";
    }

    private void createAndSaveUser(RegistrationRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(roleRepository.findByName("USER")))
                .status("offline")
                .dateRegistration(LocalDate.now())
                .build();
        userRepository.save(user);
    }

    /* ==================================================================== */
    // Логика /login

    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            authenticateUser(loginRequest);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            throw ex;
        }
        String accessToken = tokenService.issueAccessToken(loginRequest.getUsername());
        String refreshToken = tokenService.issueRefreshToken(loginRequest.getUsername());

        updateUserTokenRefresh(loginRequest.getUsername());
        addRefreshTokenCookie(refreshToken, response);

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
        String accessToken = jwtCore.getJwtFromHeader(request);
        tokenService.revokeToken(jwtCore.getJti(accessToken));
        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }

    // Отзыв Refresh Токена и очистка куки
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

    //под-метод для удаления куки
    private void clearCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setValue(null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}

