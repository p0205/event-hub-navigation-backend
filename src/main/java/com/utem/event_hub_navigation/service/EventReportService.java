package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.util.List;

import com.utem.event_hub_navigation.dto.EventArticleDTO;
import com.utem.event_hub_navigation.dto.EventReportOverviewDTO;
import com.utem.event_hub_navigation.model.EventReport;
import com.utem.event_hub_navigation.model.ReportType;

public interface EventReportService {

    List<EventReport> getEventReport(Integer eventId);

    void storeReport(Integer eventId, ReportType reportType) throws IOException;


    EventReportOverviewDTO getEventReportOverviewDTO(Integer eventId);

    
    // String generateEventArticle(Integer eventId);

    String generateEventArticle(
        Integer eventId,
        String organizingBody,
        String creditIndividuals,
        String eventObjectives,
        String activitiesConducted,
        String targetAudience,
        String perceivedImpact,
        String acknowledgements,
        String appreciationMessage,
        String language
    );
}