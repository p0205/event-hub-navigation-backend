package com.utem.event_hub_navigation.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.utem.event_hub_navigation.utils.JwtTokenUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractJwtFromRequest(request);
        System.out.println("Extracted JWT Token: " + token);
        if (token != null && JwtTokenUtil.validateToken(token)) {
            String email = JwtTokenUtil.getEmailFromToken(token);
            String role = JwtTokenUtil.getRoleFromToken(token); // Implement this method if needed

            List<SimpleGrantedAuthority> authorities = Collections
                    .singletonList(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null,
                    authorities);

            // attaches extra request-related info (like IP address and session ID) to the
            // authenticated user’s token,
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // set the context to (already) authenticated
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + token);
        // Extract token if it starts with "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

}
