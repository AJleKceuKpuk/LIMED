package com.limed_backend.security.service;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updateOnlineStatus(Long userId, String status) {
        System.out.println("Update Status userID: " +  userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User fot found, id: " + userId));
        user.setStatus(status);
        userRepository.save(user);
    }

    public void updateLastActivity(Long userId, LocalDateTime activityTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found, id: " + userId));
        user.setLastActivity(activityTime);
        userRepository.save(user);
        System.out.println("Updated lastActivity for userID: " + userId + " to " + activityTime);
    }

}