package com.limed_backend.security.service;

import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public void updateStatus(Long userId, String Status) {
        System.out.println("updateOnlineStatus");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User fot found, id: " + userId));
        user.setStatus(Status);
        userRepository.save(user);
    }
}