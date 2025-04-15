package com.limed_backend.security.service;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updateOnlineStatus(Long userId, boolean onlineStatus) {
        System.out.println("updateOnlineStatus");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User fot found, id: " + userId));
        user.setOnline(onlineStatus);
        userRepository.save(user);
    }
}