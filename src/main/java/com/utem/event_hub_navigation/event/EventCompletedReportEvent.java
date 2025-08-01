package com.utem.event_hub_navigation.event;

import org.springframework.context.ApplicationEvent;

public class EventCompletedReportEvent extends ApplicationEvent{

    private final Integer eventId;

    public EventCompletedReportEvent(Object source, Integer eventId){
        super(source);
        this.eventId = eventId;
    }

    public Integer getEventId(){
        return eventId;
    }
}
