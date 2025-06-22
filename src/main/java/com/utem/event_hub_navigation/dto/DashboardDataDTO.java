package com.utem.event_hub_navigation.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDataDTO {
    private Summary summary;
    private List<MonthlyGrowth> monthlyGrowth;
    private List<TimeDistribution> timeDistribution;
    private List<TopVenue> topVenues;
    private List<EventType> eventTypes;

    @Data
    public static class Summary {
        private int totalUsers;
        private int totalActiveUsers;
        private int totalCompletedEvents;
    }

    @Data
    public static class MonthlyGrowth {
        private String month;
        private int events;
    }

    @Data
    public static class TimeDistribution {
        private String hour;
        private int count;
    }

    @Data
    public static class TopVenue {
        private String name;
        private int count;
    }

    @Data
    public static class EventType {
        private String name;
        private int value;
    }
} 