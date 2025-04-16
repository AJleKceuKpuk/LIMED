package com.limed_backend.security.service;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

}