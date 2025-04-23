package com.limed_backend.security.controller;


import com.limed_backend.security.config.JwtCore;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.dto.Responses.UserResponse;
import com.limed_backend.security.service.TokenService;
import com.limed_backend.security.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    @Autowired
    private JwtCore jwtCore;

    private final TokenService tokenService;

    @Autowired
    private UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        try {
            String newAccessToken = tokenService.refreshAccessToken(request);
            return ResponseEntity.ok(new TokenResponse(newAccessToken));
        } catch (JwtException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ex.getMessage());
        }
    }
}