package com.limed_backend.security.controller;

import com.limed_backend.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.dto.Auth.LoginRequest;
import com.limed_backend.security.dto.Auth.RegistrationRequest;
import com.limed_backend.security.dto.Token.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    public String registration(@RequestBody RegistrationRequest request) {
        System.out.println("/registration");
        return authService.registration(request);
    }

    @PostMapping("/login")
    public TokenResponse login(HttpServletRequest request, @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("/login");
        return authService.login(request, loginRequest, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("/logout");
        return authService.logout(request, response);
    }
}
