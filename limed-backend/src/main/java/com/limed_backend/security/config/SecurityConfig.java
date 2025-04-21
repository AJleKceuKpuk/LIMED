package com.limed_backend.security.config;

import com.limed_backend.security.repository.BlockingRepository;
import com.limed_backend.security.service.ImplUserDetailsService;
import com.limed_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ImplUserDetailsService userDetailsService;

    @Autowired
    private BlockingRepository blockingRepository;
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public BanCheckFilter banCheckFilter(UserService userService, BlockingRepository blockingRepository) {
        return new BanCheckFilter(userService, blockingRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/start", "/login", "/ws/**", "/registration", "/token/**").permitAll()
                        .requestMatchers("/game", "/logout", "/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // Регистрируем BanCheckFilter после jwtAuthenticationFilter
                .addFilterAfter(banCheckFilter(null, null), JwtAuthenticationFilter.class);
        // В данном вызове выше параметры будут проигнорированы – важно, чтобы бин BanCheckFilter
        // был зарегистрирован через метод banCheckFilter(UserService, BlockingRepository)
        return http.build();
    }

    // Объявляем бин с параметрами, чтобы Spring сам нашёл необходимые зависимости

}
