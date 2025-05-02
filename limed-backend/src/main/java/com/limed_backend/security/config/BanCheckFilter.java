package com.limed_backend.security.config;

import com.limed_backend.security.entity.Blocking;
import com.limed_backend.security.entity.User;
import com.limed_backend.security.repository.BlockingRepository;
import com.limed_backend.security.service.BlockingService;
import com.limed_backend.security.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

@Component
public class BanCheckFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final BlockingService blockingService;


    public BanCheckFilter(UserService userService, BlockingService blockingService) {
        this.userService = userService;
        this.blockingService = blockingService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/game")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                filterChain.doFilter(request, response);
                return;
            }
            User user = userService.findUserByUsername(auth.getName());
            if (user != null) {
                List<Blocking> activeBlocks = blockingService.allBlockings(user, "ban");
                if (activeBlocks.stream().findAny().isPresent()) {
                    Blocking banRecord = activeBlocks.get(0);
                    String banReason = banRecord.getReason();
                    response.setContentType("text/plain; charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Аккаунт заблокирован. Причина: " + banReason);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

}
