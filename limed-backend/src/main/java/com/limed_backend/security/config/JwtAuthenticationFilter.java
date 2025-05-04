package com.limed_backend.security.config;

import com.limed_backend.security.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.limed_backend.security.entity.Token;
import com.limed_backend.security.service.ImplUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;



@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtCore jwtCore;
    @Autowired
    private ImplUserDetailsService customUserDetailsService;
    @Autowired
    private TokenService tokenService;


    public JwtAuthenticationFilter() {

    }

    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Если запрос идёт на энд-поинт обновления токена, пропускаем проверку
        if ("/token/refresh".equals(request.getServletPath()) ||
                "/logout".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtCore.getJwtFromHeader(request);

        if (StringUtils.hasText(jwt) && jwtCore.validateToken(jwt, null)) {

            String jti = jwtCore.getJti(jwt);
            Token tokenRecord = tokenService.getTokenByJti(jti);
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
