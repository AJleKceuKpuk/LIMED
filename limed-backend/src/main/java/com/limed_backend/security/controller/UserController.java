package com.limed_backend.security.controller;

import com.limed_backend.security.dto.TokenResponse;
import com.limed_backend.security.dto.UpdateEmailRequest;
import com.limed_backend.security.dto.UpdatePasswordRequest;
import com.limed_backend.security.dto.UpdateUsernameRequest;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.jwt.JwtCore;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.AuthService;
import com.limed_backend.security.service.TokenService;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PutMapping("/update-username")
    public ResponseEntity<TokenResponse> updateUsername(@RequestBody UpdateUsernameRequest request,
                                                        Authentication authentication,
                                                        HttpServletResponse response) {
        String currentUsername = authentication.getName();
        TokenResponse tokenResponse = userService.updateUsername(currentUsername, request, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request,
                                              Authentication authentication) {
        String currentUsername = authentication.getName();

        String result = userService.updateEmail(currentUsername, request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                 Authentication authentication) {
        String currentUsername = authentication.getName();
        String resultMessage = userService.updatePassword(currentUsername, request);
        return ResponseEntity.ok(resultMessage);
    }

}
