package com.limed_backend.security.controller.User;

import com.limed_backend.security.dto.Token.TokenResponse;
import com.limed_backend.security.dto.User.UpdateEmailRequest;
import com.limed_backend.security.dto.User.UpdatePasswordRequest;
import com.limed_backend.security.dto.User.UpdateUsernameRequest;
import com.limed_backend.security.dto.User.UserProfileResponse;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /** Просмотр профиля пользователя*/
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        System.out.println("/profile");
        UserProfileResponse user = userService.getProfile(authentication);
        return ResponseEntity.ok(user);
    }

     /** Изменение имени пользователя*/
    @PutMapping("/update-username")
    public ResponseEntity<TokenResponse> updateUsername(HttpServletRequest request, @RequestBody UpdateUsernameRequest userRequest,
                                                        Authentication authentication,
                                                        HttpServletResponse response) {
        System.out.println("/update-username");
        TokenResponse tokenResponse = userService.updateUsername(request, authentication.getName(), userRequest, response);
        return ResponseEntity.ok(tokenResponse);
    }

    /** Изменение Email пользователя*/
    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request,
                                              Authentication authentication) {
        System.out.println("/update-email");
        String result = userService.updateEmail(authentication.getName(), request);
        return ResponseEntity.ok(result);
    }

    /** Изменение Пароля пользователя*/
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                 Authentication authentication) {
        System.out.println("/update-password");
        String resultMessage = userService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok(resultMessage);
    }

}
