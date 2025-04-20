package com.limed_backend.security.controller;

import com.limed_backend.security.dto.*;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.mapper.UserMapper;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.AdminService;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/edit-username/{id}")
    public ResponseEntity<String> editUsername(@RequestBody UpdateUsernameRequest request,
                                              @PathVariable Long id) {
        String result = adminService.editUsername(request, id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit-email/{id}")
    public ResponseEntity<String> editEmail(@RequestBody UpdateEmailRequest request,
                                               @PathVariable Long id) {
        String result = adminService.editEmail(request, id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/edit-role/{id}")
    public ResponseEntity<String> editRole(@RequestBody UpdateRoleRequest request,
                                           @PathVariable Long id) {
        String result = adminService.editRole(request, id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/give-muted/{id}")
    public ResponseEntity<String> giveMuted(@RequestBody GiveMutedRequest request,
                                            @PathVariable Long id){
        String result = adminService.giveMuted(request, id);
        return ResponseEntity.ok(result);
    }


//    @PostMapping("/give-muted")
//    @PostMapping("/give-ban")
//    @PutMapping("/unban")
//    @PutMapping("/unmuted")

}