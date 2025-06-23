package com.utem.event_hub_navigation.dto;

import lombok.Data;
 
@Data
public class PasswordUpdateDTO {
    private String currentPassword;
    private String newPassword;
    private String outsiderNewPassword;
} 