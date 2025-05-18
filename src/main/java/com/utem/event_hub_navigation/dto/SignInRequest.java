package com.utem.event_hub_navigation.dto;

import lombok.Data;

@Data
public class SignInRequest {

    private String email;
    private String rawPassword;
}
