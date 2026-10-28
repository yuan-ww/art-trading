package com.example.demo.config;

import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class DisabledUserFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public DisabledUserFilter(UserRepository users) { this.users = users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            try {
                Integer id = Integer.valueOf(auth.getName());
                if (users.findById(id).map(user -> user.isDisabled()).orElse(true)) {
                    if (request.getSession(false) != null) request.getSession(false).invalidate();
                    SecurityContextHolder.clearContext(); response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return;
                }
            } catch (NumberFormatException ignored) { }
        }
        chain.doFilter(request, response);
    }
}
