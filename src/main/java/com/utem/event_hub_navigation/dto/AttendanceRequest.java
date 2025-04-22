package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AttendanceRequest {
    private Integer userId;
    private String qrData;
}
