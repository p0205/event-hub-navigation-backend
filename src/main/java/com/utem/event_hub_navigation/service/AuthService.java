package com.utem.event_hub_navigation.service;

import org.springframework.security.core.AuthenticationException;

import com.utem.event_hub_navigation.dto.SignInRequest;

public interface AuthService {

    String signIn(SignInRequest request) throws AuthenticationException;

}