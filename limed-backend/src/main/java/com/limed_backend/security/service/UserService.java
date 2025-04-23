package com.limed_backend.security.service;

import com.limed_backend.security.dto.Requests.UpdateEmailRequest;
import com.limed_backend.security.dto.Requests.UpdatePasswordRequest;
import com.limed_backend.security.dto.Requests.UpdateUsernameRequest;
import com.limed_backend.security.dto.Responses.FriendResponse;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.entity.Friends;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.mapper.FriendsMapper;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.FriendRepository;
import com.limed_backend.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private FriendRepository friendRepository;

    @Autowired UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final FriendsMapper friendMapper;

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

    public String addFriend(Authentication authentication, Long id){
        User currentUser = findUserByUsername(authentication.getName());
        findUserbyId(id);
        if (currentUser.getId().equals(id)){
            return "Вы не можете себе отправь дружбу";
        }
        if (friendRepository.findByUser_IdAndFriend_Id(currentUser.getId(), id).isPresent() ||
                friendRepository.findByUser_IdAndFriend_Id(id, currentUser.getId()).isPresent()){
            return "Пользователь является вашим другом";
        }
        Friends friends = Friends.builder()
                .user(currentUser)
                .friend(findUserbyId(id))
                .status("Pending")
                .build();
        friendRepository.save(friends);
        return "Предложение подружиться отправлено";
    }

    public String acceptFriend(Authentication authentication, Long senderId) {
        User currentUser = findUserByUsername(authentication.getName());
        findUserbyId(senderId);
        Friends friendInvitation = friendRepository
                .findByUser_IdAndFriend_Id(senderId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "У Вас нет предложений для дружбы от пользователя с id " + senderId));

        // Проверка: если заявка уже принята, выбрасываем исключение или возвращаем уведомление об этом
        if ("Accepted".equals(friendInvitation.getStatus())) {
            return "Дружба уже была принята ранее";
        }

        // Если заявка ещё в состоянии ожидания, меняем статус на "Accepted"
        friendInvitation.setStatus("Accepted");
        friendRepository.save(friendInvitation);

        return "Предложение принято";
    }

    public String canselFriend(Authentication authentication, Long senderId){
        User currentUser = findUserByUsername(authentication.getName());
        findUserbyId(senderId);
        Friends friendInvitation = friendRepository
                .findByUser_IdAndFriend_Id(senderId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "У Вас нет предложений для дружбы от пользователя с id " + findUserbyId(senderId).getUsername()));
        if ("Accepted".equals(friendInvitation.getStatus())) {
            return "Дружба уже была принята ранее";
        }

        friendRepository.delete(friendInvitation);
        return "Предложение отклонено";
    }

    public String deleteFriend(Authentication authentication, Long senderId){
        User currentUser = findUserByUsername(authentication.getName());
        findUserbyId(senderId);
        Friends friendInvitation = friendRepository
                .findByUser_IdAndFriend_IdAndStatus(senderId, currentUser.getId(), "Accepted")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь не является Вашим другом: " + findUserbyId(senderId).getUsername()));


        friendRepository.delete(friendInvitation);
        return "Пользователь удален из списка друзей";
    }

    public List<FriendResponse> getAcceptedFriends(Authentication authentication) {
        // Находим текущего пользователя (например, по username из token-а)
        User currentUser = findUserByUsername(authentication.getName());

        // Получаем все записи дружбы, где текущий пользователь участвует, и статус "Accepted"
        List<Friends> friendships = friendRepository.findAcceptedFriendsForUser(currentUser.getId());

        // Преобразуем каждую запись в FriendResponse, передавая ID текущего пользователя
        return friendships.stream()
                .map(friendship -> friendMapper.toFriendResponse(friendship, currentUser.getId()))
                .collect(Collectors.toList());
    }

}
