package com.limed_backend.security.preload;

import com.limed_backend.security.entity.Role;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.RoleRepository;
import com.limed_backend.security.repository.UserRepository;
import com.limed_backend.security.service.RoleCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializerUser implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCacheService roleCache;

    @Override
    public void run(String... args) {
        Role userRole = roleCache.getRole("USER");
        Role adminRole = roleCache.getRole("ADMIN");

        // Создаем множество ролей и добавляем нужные роли
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .username("ADMIN")
                    .email("admin@limed.by")
                    .password(passwordEncoder.encode("123"))
                    .roles(roles)
                    .status("offline")
                    .dateRegistration(LocalDate.now())
                    .build();

            userRepository.save(user);
        }
    }
}

