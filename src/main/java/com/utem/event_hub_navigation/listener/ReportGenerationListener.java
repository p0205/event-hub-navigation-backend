package com.utem.event_hub_navigation.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.utem.event_hub_navigation.EventCompletedReportEvent;
import com.utem.event_hub_navigation.service.EventReportService;

@Component
public class ReportGenerationListener {

    private final EventReportService reportService; // Your service for generating reports

    public ReportGenerationListener(EventReportService reportService) {
        this.reportService = reportService;
    }

    @Async("jobExecutor") // Ensures this listener runs in a separate thread
    @EventListener
    public void handleEventCompleted(EventCompletedReportEvent event) {
        System.out.println("Received event completion for event ID: " + event.getEventId() + ". Generating report...");
        try {
            reportService.storeReport(event.getEventId());

            System.out.println("Report generated successfully for event ID: " + event.getEventId());
        }catch (Exception e) {
            System.err.println("Error generating report for event ID: " + event.getEventId() + " - " + e.getMessage());
            // Implement error handling and potentially retry mechanisms
        }
    }
}