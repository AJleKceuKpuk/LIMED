package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.*;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.EmailAlreadyExistsException;
import com.limed_backend.security.exception.InvalidOldPasswordException;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.exception.UsernameAlreadyExistsException;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    // Создаем сущность пользователя во время регистрации
    public void createAndSaveUser(RegistrationRequest request) {
        Role userRole = roleService.getRole("USER");

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

    // Поиск пользователя имени
    @Cacheable(value = "userCache", key = "#username")
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    // Поиск пользователя по Id
    @Cacheable(value = "userCache", key = "#id")
    public User findUserById(Long id){
        return userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    // Проверка доступности имени
    public void validateUsernameAvailability(String newUsername) {
        Optional<User> userExists = userRepository.findByUsernameIgnoreCase(newUsername);
        if (userExists.isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
    }

    // Проверка доступности Email
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

    // обновляем в Redis нашего user
    private void updateUserCache(User user) {
        Cache userCache = cacheManager.getCache("userCache");
        userCache.evict(user.getId());
        userCache.evict(user.getUsername());
        userCache.put(user.getId(), user);
        userCache.put(user.getUsername(), user);
    }

    // обновление статуса пользователя в БД
    public void updateUserStatus(Long userId, String newStatus) {
        User user = findUserById(userId);
        if (newStatus.equals(user.getStatus())) {
            return;
        }
        user.setStatus(newStatus);
        userRepository.save(user);
        updateUserCache(user);
    }

    //обновление последней активности в БД
    public void updateLastActivity(Long userId, LocalDateTime activityTime) {
        User user = findUserById(userId);
        user.setLastActivity(activityTime);
        userRepository.save(user);
        updateUserCache(user);
    }

    // Изменение имени пользователя
    public TokenResponse updateUsername(HttpServletRequest request,
                                        String currentUsername,
                                        UpdateUsernameRequest userRequest,
                                        HttpServletResponse response) {
        User user = findUserByUsername(currentUsername);
        validateUsernameAvailability(userRequest.getNewUsername());
        tokenService.revokeAllTokens(user.getUsername());
        user.setUsername(userRequest.getNewUsername());
        userRepository.save(user);
        updateUserCache(user);
        return tokenService.generateAndSetTokens(request, user, response);
    }

    //Изменение Email пользователя
    public String updateEmail(String currentUsername, UpdateEmailRequest request) {
        User user = findUserByUsername(currentUsername);
        validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        updateUserCache(user);
        return "Email успешно обновлён";
    }

    // Изменение пароля
    public String updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = findUserByUsername(currentUsername);

        validateOldPassword(user, request.getOldPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Пароль успешно обновлён";
    }

    // Проверка пользователя
    public void authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
    }

    public boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
    }
}

