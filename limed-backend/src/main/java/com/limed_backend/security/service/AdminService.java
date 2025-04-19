package com.limed_backend.security.service;

import com.limed_backend.security.dto.UserResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserResponse(user);
    }
}
