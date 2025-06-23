package com.utem.event_hub_navigation.service;

import java.time.LocalDateTime;

import com.utem.event_hub_navigation.dto.DashboardDataDTO;

public interface AdminService {
    DashboardDataDTO getDashboardData(LocalDateTime startDateTime, LocalDateTime endDateTime) ;
}
