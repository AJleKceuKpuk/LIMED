package com.limed_backend.security.config;

import com.limed_backend.security.repository.BlockingRepository;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.repository.TokenRepository;
import com.limed_backend.security.service.ImplUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtCore jwtCore;
    @Autowired
    private ImplUserDetailsService customUserDetailsService;
    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BlockingRepository blockingRepository;
;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Если запрос идёт на эндпоинт обновления токена, пропускаем проверку
        if ("/token/refresh".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }
        if ("/logout".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtCore.getJwtFromHeader(request);

        if (StringUtils.hasText(jwt) && jwtCore.validateToken(jwt, null)) {

            String jti = jwtCore.getJti(jwt);
            Token tokenRecord = tokenRepository.findByJti(jti);
            if (tokenRecord == null || tokenRecord.getRevoked() || tokenRecord.getExpiration().before(new Date())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token is revoked or invalid.");
                return;
            }
            String username = jwtCore.getUsernameFromToken(jwt);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }


}
