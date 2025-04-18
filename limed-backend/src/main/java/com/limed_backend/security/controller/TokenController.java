package com.limed_backend.security.controller;


import com.limed_backend.security.dto.AccessTokenResponse;
import com.limed_backend.security.service.TokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;


    //@RequestHeader(name = "Authorization", required = false) String authHeader
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        try {
            String newAccessToken = tokenService.refreshAccessToken(request);
            return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
        } catch (JwtException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ex.getMessage());
        }
    }
}