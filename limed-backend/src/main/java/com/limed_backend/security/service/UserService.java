package com.limed_backend.security.service;

import com.limed_backend.security.dto.Auth.RegistrationRequest;
import com.limed_backend.security.dto.Token.TokenResponse;
import com.limed_backend.security.dto.User.*;
import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.exceprions.EmailAlreadyExistsException;
import com.limed_backend.security.exception.exceprions.InvalidOldPasswordException;
import com.limed_backend.security.exception.exceprions.UsernameAlreadyExistsException;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RoleCacheService roleCache;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCache;
    private final UserMapper userMapper;

    /** Проверка доступности username*/
    public void validateUsernameAvailability(String newUsername) {
        Optional<User> userExists = userRepository.findByUsernameIgnoreCase(newUsername);
        if (userExists.isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
    }
    /** Проверка доступности Email*/
    public void validateEmailAvailability(String newEmail) {
        Optional<User> emailExists = userRepository.findByEmailIgnoreCase(newEmail);
        if (emailExists.isPresent()) {
            throw new EmailAlreadyExistsException();
        }
    }
    /** Проверка пароля*/
    public void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOldPasswordException();
        }
    }
    /**ONLY ADMIN Проверка на роль Admin*/
    public boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
    }


    /** Получение профиля текущего пользователя*/
    public UserProfileResponse getProfile(Authentication authentication) {
        User user = userCache.findUserByUsername(authentication.getName());
        return userMapper.toUserProfileResponse(user);
    }
    /** Получение пользователя по его Id Только для Admin*/
    public UserResponse getUser(Long id){
        User user = userCache.findUserById(id);
        return userMapper.toUserResponse(user);
    }
    /** ONLY ADMIN Получение всех пользователей постранично*/
    public Page<UserResponse> getAllUsers(int size, int page){
        Pageable pageable = PageRequest.of(page, size, Sort.by("username"));
        Page<User> users = userRepository.getAllUsers(pageable);
        return users.map(userMapper::toUserResponse);
    }


    /** Создаем сущность пользователя во время регистрации*/
    public void createAndSaveUser(RegistrationRequest request) {
        Role userRole = roleCache.getRole("USER");

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
    /** Обновление статуса пользователя в БД*/
    public void updateUserStatus(Long userId, String newStatus) {
        User user = userCache.findUserById(userId);
        if (newStatus.equals(user.getStatus())) {
            return;
        }
        user.setStatus(newStatus);
        userRepository.save(user);
        userCache.deleteUserCache(user);
        userCache.addUserCache(user);
    }
    /** Обновление последней активности в БД*/
    public void updateLastActivity(Long userId, LocalDateTime activityTime) {
        User user = userCache.findUserById(userId);
        user.setLastActivity(activityTime);
        userRepository.save(user);
        userCache.deleteUserCache(user);
        userCache.addUserCache(user);
    }
    /** Изменение username пользователя*/
    public TokenResponse updateUsername(HttpServletRequest request,
                                        String currentUsername,
                                        UpdateUsernameRequest userRequest,
                                        HttpServletResponse response) {
        User user = userCache.findUserByUsername(currentUsername);
        validateUsernameAvailability(userRequest.getNewUsername());
        tokenService.revokeAllTokens(user.getUsername());

        userCache.deleteUserCache(user);
        user.setUsername(userRequest.getNewUsername());
        userRepository.save(user);
        userCache.addUserCache(user);

        return tokenService.generateAndSetTokens(request, user, response);
    }
    /** ONLY ADMIN изменение username пользователя принудительно */
    @Transactional
    public String editUsername(UpdateUsernameRequest request, Long id){
        User user = userCache.findUserById(id);

        validateUsernameAvailability(request.getNewUsername());
        userCache.deleteUserCache(user);
        tokenService.revokeAllTokens(user.getUsername());
        user.setUsername(request.getNewUsername());
        userCache.addUserCache(user);

        userRepository.save(user);
        return "Имя игрока изменено";
    }
    /** Изменение Email пользователя*/
    public String updateEmail(String currentUsername, UpdateEmailRequest request) {
        User user = userCache.findUserByUsername(currentUsername);
        validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        userCache.deleteUserCache(user);
        userCache.addUserCache(user);
        return "Email успешно обновлён";
    }
    /** ONLY ADMIN Изменение Email пользователя принудительно */
    @Transactional
    public String editEmail(UpdateEmailRequest request, Long id){
        User user = userCache.findUserById(id);
        validateEmailAvailability(request.getNewEmail());
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        userCache.deleteUserCache(user);
        userCache.addUserCache(user);
        return "Email игрока сохранен";
    }
    /** Изменение Password пользователя*/
    public String updatePassword(String currentUsername, UpdatePasswordRequest request) {
        User user = userCache.findUserByUsername(currentUsername);

        validateOldPassword(user, request.getOldPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Пароль успешно обновлён";
    }
    /** ONLY ADMIN Редактирование Ролей пользователя */
    @Transactional
    public String editRole(UpdateRoleRequest request) {
        User user = userCache.findUserById(request.getId());
        System.out.println(user.toString());
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleCache.getRole(roleName);
            newRoles.add(role);
        }
        userCache.deleteUserCache(user);
        user.setRoles(newRoles);
        userCache.addUserCache(user);
        userRepository.save(user);
        return "Роли пользователя успешно обновлены!";
    }

}

