package com.utem.event_hub_navigation.service.impl;

import com.utem.event_hub_navigation.dto.DashboardDataDTO;
import com.utem.event_hub_navigation.model.AccountStatus;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepo userRepo;
    private final EventRepo eventRepo;

    @Autowired
    public AdminServiceImpl(UserRepo userRepo, EventRepo eventRepo) {
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
    }

    @Override
    public DashboardDataDTO getDashboardData(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        // If no start date is provided, default to the first day of the current month
        if (startDateTime == null) {
            startDateTime = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        DashboardDataDTO dto = new DashboardDataDTO();
        DashboardDataDTO.Summary summary = new DashboardDataDTO.Summary();
        summary.setTotalUsers((int) userRepo.countAllUsers());
        summary.setTotalActiveUsers((int) userRepo.countByStatus(AccountStatus.ACTIVE));


        summary.setTotalCompletedEvents((int) eventRepo.countByStatus(EventStatus.COMPLETED,startDateTime,endDateTime));
        dto.setSummary(summary);
       

        // Monthly Growth
        List<Object[]> monthlyGrowthRaw = eventRepo.countEventsByMonth(startDateTime,endDateTime);
        List<DashboardDataDTO.MonthlyGrowth> monthlyGrowth = new ArrayList<>();
        for (Object[] row : monthlyGrowthRaw) {
            DashboardDataDTO.MonthlyGrowth mg = new DashboardDataDTO.MonthlyGrowth();

            mg.setMonth((String) row[0]);
            mg.setEvents(((Number) row[1]).intValue());
            monthlyGrowth.add(mg);
        }
        dto.setMonthlyGrowth(monthlyGrowth);

        // Time Distribution
        List<Object[]> timeDistRaw = eventRepo.countSessionsByHour(startDateTime,endDateTime);
        List<DashboardDataDTO.TimeDistribution> timeDist = new ArrayList<>();
        for (Object[] row : timeDistRaw) {
            DashboardDataDTO.TimeDistribution td = new DashboardDataDTO.TimeDistribution();
            td.setHour((String) row[0]);
            td.setCount(((Number) row[1]).intValue());
            timeDist.add(td);
        }
        dto.setTimeDistribution(timeDist);

        // Top Venues
        List<Object[]> topVenuesRaw = eventRepo.findTop5VenuesByEventCount(startDateTime,endDateTime);
        List<DashboardDataDTO.TopVenue> topVenues = new ArrayList<>();
        for (Object[] row : topVenuesRaw) {
            DashboardDataDTO.TopVenue tv = new DashboardDataDTO.TopVenue();
            tv.setName((String) row[0]);
            tv.setCount(((Number) row[1]).intValue());
            topVenues.add(tv);
        }
        dto.setTopVenues(topVenues);

        // Event Types
        List<Object[]> eventTypesRaw = eventRepo.countEventsByType(startDateTime,endDateTime);
        List<DashboardDataDTO.EventType> eventTypes = new ArrayList<>();
        for (Object[] row : eventTypesRaw) {
            DashboardDataDTO.EventType et = new DashboardDataDTO.EventType();
            et.setName(row[0].toString());
            et.setValue(((Number) row[1]).intValue());
            eventTypes.add(et);
        }
        dto.setEventTypes(eventTypes);

        return dto;
    }
}
