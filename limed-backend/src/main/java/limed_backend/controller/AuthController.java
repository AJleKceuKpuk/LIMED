package limed_backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import limed_backend.jwt.JwtUtil;
import limed_backend.dto.LoginRequest;
import limed_backend.dto.RegistrationRequest;
import limed_backend.dto.TokenResponse;
import limed_backend.models.Role;
import limed_backend.models.User;
import limed_backend.repository.RoleRepository;
import limed_backend.repository.UserRepository;
import limed_backend.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;

@RestController
@RequestMapping
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/start")
    public String welcome() {
        return "Добро пожаловать! Доступно всем.";
    }

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // 1. Аутентификация по логину и паролю
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Генерация и сохранение токенов через TokenService
        String accessToken = tokenService.issueAccessToken(loginRequest.getUsername());
        String refreshToken = tokenService.issueRefreshToken(loginRequest.getUsername());

        // 3. Создание httpOnly cookie для refresh token
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);   // недоступно для JavaScript
        refreshTokenCookie.setSecure(true);       // передается только по HTTPS
        refreshTokenCookie.setPath("/");          // доступно для всего приложения

        // 4. Вычисление времени жизни куки, исходя из срока действия refresh token
        Date refreshExpiration = jwtUtil.getExpirationFromToken(refreshToken);
        int maxAge = (int) ((refreshExpiration.getTime() - System.currentTimeMillis()) / 1000);
        refreshTokenCookie.setMaxAge(maxAge);

        // 5. Добавление куки в ответ
        response.addCookie(refreshTokenCookie);

        // 6. Возврат access token в теле ответа
        return new TokenResponse(accessToken);
    }

    @PostMapping("/registration")
    public String register(@RequestBody RegistrationRequest request) {
        // Если пользователь с таким именем уже существует
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Пользователь с таким именем уже существует";
        }

        // Поиск роли "USER" в БД
        Role roleUser = roleRepository.findByName("USER");
        if (roleUser == null) {
            return "Роль USER не найдена. Обратитесь к администратору.";
        }

        // Регистрация нового пользователя с ролью USER
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(roleUser))
                .build();
        userRepository.save(user);
        return "Пользователь зарегистрирован";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Обработка refresh token из cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    try {
                        String refreshToken = cookie.getValue();
                        String refreshJti = jwtUtil.getJti(refreshToken);
                        // Отзыв refresh token через TokenService
                        tokenService.revokeToken(refreshJti);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // Очистка куки
                    cookie.setValue(null);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        // Обработка access token из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String accessJti = jwtUtil.getJti(accessToken);
                tokenService.revokeToken(accessJti);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }
}
