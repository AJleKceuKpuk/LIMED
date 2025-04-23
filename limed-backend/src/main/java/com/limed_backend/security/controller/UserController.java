package com.limed_backend.security.controller;

import com.limed_backend.security.dto.Responses.FriendResponse;
import com.limed_backend.security.dto.Responses.TokenResponse;
import com.limed_backend.security.dto.Requests.UpdateEmailRequest;
import com.limed_backend.security.dto.Requests.UpdatePasswordRequest;
import com.limed_backend.security.dto.Requests.UpdateUsernameRequest;
import com.limed_backend.security.dto.Responses.UserProfileResponse;
import com.limed_backend.security.dto.Responses.UserResponse;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.exception.ResourceNotFoundException;
import com.limed_backend.security.repository.FriendRepository;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.AuthService;
import com.limed_backend.security.service.TokenService;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private FriendRepository friendRepository;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = userService.findUserByUsername(authentication.getName());
        return ResponseEntity.ok(new UserProfileResponse(user.getId(),user.getUsername(), user.getEmail(), user.getDateRegistration()));
    }

    @PutMapping("/update-username")
    public ResponseEntity<TokenResponse> updateUsername(HttpServletRequest request, @RequestBody UpdateUsernameRequest userRequest,
                                                        Authentication authentication,
                                                        HttpServletResponse response) {
        TokenResponse tokenResponse = userService.updateUsername(request, authentication.getName(), userRequest, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request,
                                              Authentication authentication) {
        String result = userService.updateEmail(authentication.getName(), request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                 Authentication authentication) {
        String resultMessage = userService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendResponse>> getAcceptedFriends(Authentication authentication) {
        List<FriendResponse> friends = userService.getAcceptedFriends(authentication);
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/add-friend/{id}")
    public ResponseEntity<String> addFriend(Authentication authentication,
                                            @PathVariable Long id){
        String resultMessage = userService.addFriend(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/accept-friend/{id}")
    public ResponseEntity<String> acceptFriend(Authentication authentication,
                                               @PathVariable Long id){
        String resultMessage = userService.acceptFriend(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/cansel-friend/{id}")
    public ResponseEntity<String> canselFriend(Authentication authentication,
                                               @PathVariable Long id){
        String resultMessage = userService.canselFriend(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }

    @DeleteMapping("/delete-friend/{id}")
    public ResponseEntity<String> deleteFriend(Authentication authentication,
                                                @PathVariable Long id){
        String resultMessage = userService.deleteFriend(authentication, id);
        return ResponseEntity.ok(resultMessage);
    }
}
