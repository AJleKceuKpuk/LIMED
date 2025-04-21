package com.limed_backend.security.controller;

import com.limed_backend.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.config.JwtCore;
import com.limed_backend.security.dto.Requests.LoginRequest;
import com.limed_backend.security.dto.Requests.RegistrationRequest;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtCore jwtCore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    @GetMapping("/start")
    public String welcome() {
        return "Добро пожаловать! Доступно всем.";
    }

    @PostMapping("/login")
    public TokenResponse login(HttpServletRequest request, @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(request, loginRequest, response);
    }

    @PostMapping("/registration")
    public String registration(@RequestBody RegistrationRequest request) {
        return authService.registration(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}
