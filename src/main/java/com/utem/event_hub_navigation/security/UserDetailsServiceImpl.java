package com.utem.event_hub_navigation.security;


import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.repo.UserRepo;

import lombok.RequiredArgsConstructor;


/*
 * 1. Login: 
 *          Authenticating user credentials at login.
 * 
 * 2. Token Validation in every request: 
 *          Validating user status and roles during token validation for secured requests
 */
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService{


    private final UserRepo userRepo;

    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.utem.event_hub_navigation.model.User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));


            return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
     
    }

}
