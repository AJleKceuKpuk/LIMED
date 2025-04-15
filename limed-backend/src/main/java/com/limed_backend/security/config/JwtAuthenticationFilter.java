package com.limed_backend.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import com.limed_backend.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtCore jwtCore;

    @Autowired
    private UserDetailsServiceImpl customUserDetailsService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Извлекаем JWT из заголовка Authorization
        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && jwtCore.validateToken(jwt)) {
            // Получаем уникальный идентификатор токена (jti)
            String jti = jwtCore.getJti(jwt);

            // Извлекаем запись токена из базы данных
            Token tokenRecord = tokenRepository.findByJti(jti);

            // Если записи нет, токен отозван или истёк – отклоняем запрос
            if (tokenRecord == null
                    || tokenRecord.getRevoked()
                    || tokenRecord.getExpiration().before(new Date())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token is revoked or invalid.");
                return;
            }

            // Если токен валидный, продолжаем обработку
            String username = jwtCore.getUsernameFromToken(jwt);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
