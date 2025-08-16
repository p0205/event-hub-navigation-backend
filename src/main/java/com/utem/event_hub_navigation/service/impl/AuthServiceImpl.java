package com.utem.event_hub_navigation.service.impl;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.SignInRequest;
import com.utem.event_hub_navigation.service.AuthService;
import com.utem.event_hub_navigation.utils.JwtTokenUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    @Override
    public String signIn(SignInRequest request) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(request.getEmail(),
                request.getRawPassword());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException e) {
            // rethrow authentication exceptions with message
            throw e;
        } catch (Exception e) {
            // wrap other exceptions if needed
            throw new AuthenticationException("Authentication failed", e) {
            };
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extracting principal (username)
        String username;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal())
                    .getUsername();
        } else {
            username = authentication.getName(); // Fallback
        }

        // Extracting roles (authorities)
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JwtTokenUtil.generateToken(username, roles);
    }
}
