package com.limed_backend.security.config;

import com.limed_backend.security.service.TokenCacheService;
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
    @Autowired
    private TokenCacheService tokenCache;


    public JwtAuthenticationFilter() {

    }

    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getServletPath();
        String jwt = jwtCore.getJwtFromHeader(request);

        if ("/logout".equals(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isRefreshEndpoint = "/token/refresh".equals(requestPath);
        boolean valid;

        if (isRefreshEndpoint) {

            valid = jwtCore.validateTokenIgnoringExpiration(jwt);
        } else {
            valid = jwtCore.validateToken(jwt, null);
        }

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Token is invalid according to validations.");
            return;
        }

        String jti = jwtCore.getJti(jwt);
        Token tokenRecord = tokenCache.getTokenByJti(jti);
        if (tokenRecord == null || tokenRecord.getRevoked() ||
                (!isRefreshEndpoint && tokenRecord.getExpiration().before(new Date()))) {
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

        filterChain.doFilter(request, response);
    }



}
