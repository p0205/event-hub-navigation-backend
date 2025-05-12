package com.utem.event_hub_navigation.scheduler;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.utem.event_hub_navigation.EventCompletedReportEvent;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.service.EventService;

@Component
public class EventCompletionScheduler {

    private final EventService eventService; 
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public EventCompletionScheduler(EventService eventService, ApplicationEventPublisher eventPublisher) {
        this.eventService = eventService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Example: Runs every hour at the beginning of the hour
    // Other options: fixedRate, fixedDelay
    @Transactional
    public void markEventsAsCompleted() {
        List<Event> overdueEvents = eventService.getOverdueActiveEvents();
        for (Event event : overdueEvents) {
            event.setStatus(EventStatus.COMPLETED);
            eventService.markEventsAsCompleted(event);
            // Publish an event to signal completion
            eventPublisher.publishEvent(new EventCompletedReportEvent(this, event.getId()));
            System.out.println("Event marked as completed: " + event.getId());
        }
    }
}
