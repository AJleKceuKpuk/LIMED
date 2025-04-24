package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.UpdateEmailRequest;
import com.limed_backend.security.dto.Requests.UpdatePasswordRequest;
import com.limed_backend.security.dto.Requests.UpdateUsernameRequest;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.ContactsMapper;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.ContactsRepository;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final ContactsMapper friendMapper;

    //обновление статуса пользователя в БД
    public void updateOnlineStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));
        user.setStatus(status);
        userRepository.save(user);
    }

    //обновление последней активности в БД
    public void updateLastActivity(Long userId, LocalDateTime activityTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не найден"));
        user.setLastActivity(activityTime);
        userRepository.save(user);
    }

    // Поиск пользователя
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    public User findUserbyId(Long id){
        return userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    /* ==================================================================== */
    // Логика update username
    public TokenResponse updateUsername(HttpServletRequest request, String currentUsername, UpdateUsernameRequest userRequest, HttpServletResponse response) {
        User user = findUserByUsername(currentUsername);
        authService.validateUsername(userRequest.getNewUsername());
        tokenService.revokeAllTokens(user.getUsername());
        user.setUsername(userRequest.getNewUsername());
        userRepository.save(user);
        return generateAndSetTokens(request, user, response);
    }

    //регенирация и выдача токенов
    private TokenResponse generateAndSetTokens(HttpServletRequest request, User user, HttpServletResponse response) {
        String newAccessToken = tokenService.issueAccessToken(request, user.getUsername());
        String newRefreshToken = tokenService.issueRefreshToken(request, user.getUsername());
        authService.addRefreshTokenCookie(newRefreshToken, response);
        return new TokenResponse(newAccessToken);
    }

    /* ==================================================================== */
    // Логика update email

    public String updateEmail(String currentUsername, UpdateEmailRequest request) {
        User user = findUserByUsername(currentUsername);
        authService.validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return "Email успешно обновлён";
    }

    /* ==================================================================== */
    // Логика update password

    public String updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = findUserByUsername(currentUsername);
        authService.validateOldPassword(user, request.getOldPassword());
        changePassword(user, request.getNewPassword());
        return "Пароль успешно обновлён";
    }

    //Изменение пароля в БД
    private void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /* ==================================================================== */

}
