package com.limed_backend.security.controller;

import com.limed_backend.security.dto.UserResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.AdminService;
import com.limed_backend.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminService adminService;


    @GetMapping("/start")
    public String adminAccess() {
        return "Добро пожаловать, ADMIN! Здесь вы управляете системой.";
    }

    @GetMapping("/get-allusers")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse userResponse = adminService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }


//    @PutMapping("/edit-username")
//    @PutMapping("/edit-email")
//    @PutMapping("/edit-role")
//
//    @PostMapping("/give-muted")
//    @PostMapping("/give-ban")
//    @PutMapping("/unban")
//    @PutMapping("/unmuted")

}