package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventReportOverviewDTO {
    private String eventName;
    private AttendanceReportOverview attendance;
    private BudgetReportOverview budget;
    private FeedbackReportOveriew feedback;
}
/*
 * public class AttendanceReportOverview {
    private boolean generated;
    private String generatedDate;
    private List<SessionAttendanceDTO> sessionAttendances = new ArrayList<>();
}
    public class BudgetReportOverview {
    private boolean generated;
    private String generatedDate;
    private double totalBudget;
    private double totalExpenses;
    private double remaining;

}
    public class FeedbackReportOveriew {
    private double averageRating;
    private int feedbackCount;
    private Map<Integer, Long> ratingBreakdown;
}
 */