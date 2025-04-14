package limed_backend.controller;

import limed_backend.component.JwtUtil;
import limed_backend.models.Role;
import limed_backend.models.User;
import limed_backend.repository.RoleRepository;
import limed_backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @GetMapping("/start")
    public String welcome() {
        return "Добро пожаловать! Доступно всем.";
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword())
        );
        // При успешной аутентификации генерируется токен
        String token = jwtUtil.generateToken(loginRequest.getUsername());
        return new TokenResponse(token);
    }

    @PostMapping("/registration")
    public String register(@RequestBody RegistrationRequest request) {
        // Если пользователь с таким именем уже существует
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Пользователь с таким именем уже существует";
        }

        // Ищем роль "USER" в базе
        Role roleUser = roleRepository.findByName("ADMIN");
        if (roleUser == null) {
            return "Роль USER не найдена. Обратитесь к администратору.";
        }

        // Регистрируем пользователя с ролью USER
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(roleUser))
                .build();
        userRepository.save(user);
        return "Пользователь зарегистрирован";
    }

    @PostMapping("/logout")
    public String logout() {
        // При использовании JWT (stateless) logout можно реализовать на клиентской стороне
        return "Вы вышли из системы";
    }

    // DTO классы для передачи данных

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class TokenResponse {
        private String token;
        public TokenResponse(String token) {
            this.token = token;
        }
    }

    @Data
    public static class RegistrationRequest {
        private String username;
        private String password;
    }
}