package com.limed_backend.security.service;

import com.limed_backend.security.dto.TokenResponse;
import com.limed_backend.security.dto.UpdateEmailRequest;
import com.limed_backend.security.dto.UpdatePasswordRequest;
import com.limed_backend.security.dto.UpdateUsernameRequest;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;

    private final PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;

    //обновление статуса пользователя в БД
    public void updateOnlineStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User fot found, id: " + userId));
        user.setStatus(status);
        userRepository.save(user);
    }

    //обновление последней активности в БД
    public void updateLastActivity(Long userId, LocalDateTime activityTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found, id: " + userId));
        user.setLastActivity(activityTime);
        userRepository.save(user);
    }

    // Поиск пользователя
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    // Проверка Имени
    public void validateUsername(String newUsername) {
        Optional<User> userExists = userRepository.findByUsername(newUsername);
        if (userExists.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин уже используется");
        }
    }

    // Проверка Email
    public void validateEmailAvailability(String newEmail) {
        Optional<User> emailExists = userRepository.findByEmail(newEmail);
        if (emailExists.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
        }
    }

    // Проверка старого пароля
    public void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный старый пароль");
        }
    }

    /* ==================================================================== */
    // Логика update username
    public TokenResponse updateUsername(String currentUsername, UpdateUsernameRequest request, HttpServletResponse response) {
        User user = findUserByUsername(currentUsername);
        validateUsername(request.getNewUsername());
        tokenService.revokeAllTokens(user.getUsername());
        user.setUsername(request.getNewUsername());
        userRepository.save(user);
        return generateAndSetTokens(user, response);
    }

    private TokenResponse generateAndSetTokens(User user, HttpServletResponse response) {
        String newAccessToken = tokenService.issueAccessToken(user.getUsername());
        String newRefreshToken = tokenService.issueRefreshToken(user.getUsername());
        authService.addRefreshTokenCookie(newRefreshToken, response);
        return new TokenResponse(newAccessToken);
    }

    /* ==================================================================== */
    // Логика update email

    public String updateEmail(String currentUsername, UpdateEmailRequest request) {
        User user = findUserByUsername(currentUsername);
        validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return "Email успешно обновлён";
    }

    /* ==================================================================== */
    // Логика update password

    public String updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = findUserByUsername(currentUsername);
        validateOldPassword(user, request.getOldPassword());
        changePassword(user, request.getNewPassword());
        return "Пароль успешно обновлён";
    }

    private void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /* ==================================================================== */
}
