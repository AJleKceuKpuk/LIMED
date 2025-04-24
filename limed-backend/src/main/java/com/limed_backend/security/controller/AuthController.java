package com.limed_backend.security.controller;

import com.limed_backend.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.dto.Requests.LoginRequest;
import com.limed_backend.security.dto.Requests.RegistrationRequest;
import com.limed_backend.security.dto.Responses.TokenResponse;
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
        return authService.registration(request);
    }

    @PostMapping("/login")
    public TokenResponse login(HttpServletRequest request, @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        System.out.println("/login");
        return authService.login(request, loginRequest, response);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}
