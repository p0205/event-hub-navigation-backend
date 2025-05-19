package com.utem.event_hub_navigation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {

    private String email;
    private String phoneNo;
    private String rawPassword;
}
