package limed_backend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import limed_backend.jwt.JwtUtil;
import limed_backend.component.LoginRequest;
import limed_backend.component.RegistrationRequest;
import limed_backend.jwt.TokenResponse;
import limed_backend.models.Role;
import limed_backend.models.User;
import limed_backend.repository.RoleRepository;
import limed_backend.repository.UserRepository;
import limed_backend.jwt.TokenBlacklistService;
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
    private  AuthenticationManager authenticationManager;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

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
        // Выполняем аутентификацию по логину и паролю
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword())
        );

        // Генерируем токены
        String accessToken = jwtUtil.generateAccessToken(loginRequest.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

        // Создаем cookie для refresh token
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);          // Доступно только сервером
        refreshTokenCookie.setSecure(true);              // Передается только по HTTPS
        refreshTokenCookie.setPath("/");                 // Cookie доступно для всего приложения
        // Можно настроить время жизни cookie (например, в секундах)
        refreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiration() / 1000));

        // Добавляем cookie в ответ
        response.addCookie(refreshTokenCookie);

        // Возвращаем access token в теле ответа
        return new TokenResponse(accessToken);
    }

    @PostMapping("/registration")
    public String register(@RequestBody RegistrationRequest request) {
        // Если пользователь с таким именем уже существует
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Пользователь с таким именем уже существует";
        }

        // Ищем роль "USER" в базе
        Role roleUser = roleRepository.findByName("USER");
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

//    @PostMapping("/logout")
//    public String logout() {
//        // При использовании JWT (stateless) logout можно реализовать на клиентской стороне
//        return "Вы вышли из системы";
//    }

//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
//        // Удаляем refresh token из httpOnly cookie, выставляя maxAge в 0
//        Cookie refreshCookie = new Cookie("refreshToken", null);
//        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(true);
//        refreshCookie.setPath("/");
//        refreshCookie.setMaxAge(0);        // Указывает браузеру удалить куку
//        response.addCookie(refreshCookie);
//
//        // Если у вас реализована серверная логика проверки токенов или хранение токенов,
//        // здесь можно реализовать деактивацию access token и refresh token на стороне сервера.
//
//        return ResponseEntity.ok("Вы вышли из системы");
//    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Обработка refresh token из cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    try {
                        String refreshToken = cookie.getValue();
                        String refreshJti = jwtUtil.getJti(refreshToken);
                        // Здесь можно тоже получить expiration refresh token, если требуется
                        Date refreshExpiry = jwtUtil.getExpirationFromToken(refreshToken);
                        if (refreshJti != null && refreshExpiry != null) {
                            tokenBlacklistService.blacklistToken(refreshJti, refreshExpiry);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
                Date accessExpiry = jwtUtil.getExpirationFromToken(accessToken);
                if (accessJti != null && accessExpiry != null) {
                    tokenBlacklistService.blacklistToken(accessJti, accessExpiry);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ResponseEntity.ok("Вы успешно вышли из системы. Токены деактивированы.");
    }
}