package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckResponse {
    private EmailCheckResult result;
    private UserDTO userDTO;  // present only if VALID_EMAIL
    // constructor, getters
}
