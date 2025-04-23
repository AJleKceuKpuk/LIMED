package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.LoginRequest;
import com.limed_backend.security.dto.Requests.RegistrationRequest;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.exception.*;
import com.limed_backend.security.config.JwtCore;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.swing.interop.SwingInterOpUtils;
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
import java.util.IllegalFormatCodePointException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    // Проверка Имени
    public void validateUsername(String newUsername) {
        Optional<User> userExists = userRepository.findByUsername(newUsername);
        if (userExists.isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
    }

    // Проверка Email
    public void validateEmailAvailability(String newEmail) {
        Optional<User> emailExists = userRepository.findByEmail(newEmail);
        if (emailExists.isPresent()) {
            throw new EmailAlreadyExistsException();
        }
    }

    // Проверка старого пароля
    public void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOldPasswordException();
        }
    }


    /* ==================================================================== */
    // Логика /registration

    public String registration(RegistrationRequest request) {
        validateUsername(request.getUsername());
        validateEmailAvailability(request.getEmail());
        createAndSaveUser(request);
        return "Пользователь зарегистрирован";
    }

    // создаем и сохраняем в бд пользователя
    private void createAndSaveUser(RegistrationRequest request) {

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Роль 'USER' не найдена"));
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(userRole))
                .status("offline")
                .dateRegistration(LocalDate.now())
                .build();
        userRepository.save(user);
    }

    /* ==================================================================== */
    // Логика /login

    public TokenResponse login(HttpServletRequest request, LoginRequest loginRequest, HttpServletResponse response) {
        try {
            authenticateUser(loginRequest);
        } catch (Exception ex) {
            throw new InvalidUsernameOrPasswordException();
        }
        String accessToken = tokenService.issueAccessToken(request, loginRequest.getUsername());
        String refreshToken = tokenService.issueRefreshToken(request, loginRequest.getUsername());
        updateUserStatus(loginRequest.getUsername(), "online");
        addRefreshTokenCookie(refreshToken, response);

        return new TokenResponse(accessToken);
    }

    // добавление refresh token в куки
    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(cookieLifetime(refreshToken));
        response.addCookie(refreshTokenCookie);
    }

    // аутентификация пользователя
    private void authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
    }

    // обновление статуса пользователя в БД
    private void updateUserStatus(String username, String status) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.updateStatusUser(status);
            userRepository.save(user);
        });
    }

    // вычисление времени жизни куки
    private int cookieLifetime(String refreshToken) {
        Date refreshExpiration = jwtCore.getExpirationFromToken(refreshToken);
        return (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    /* ==================================================================== */
    // Логика /logout
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("/logout");
        revokeRefreshTokenFromCookies(request, response);
        System.out.println("revoke refrehs");
        String accessToken = jwtCore.getJwtFromHeader(request);
        if (accessToken == null){
            return ResponseEntity.ok("Без авторизации нельзя");
        }
        System.out.println("Access " + accessToken);
        tokenService.revokeToken(jwtCore.getJti(accessToken));
        System.out.println("revoke access");
        updateUserStatus(jwtCore.getUsernameFromToken(accessToken), "offline");
        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }

    // Отзыв Refresh Токена и очистка куки
    private void revokeRefreshTokenFromCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("Cookie null");
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

