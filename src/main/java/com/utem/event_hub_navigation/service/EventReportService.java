package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.util.List;

import com.utem.event_hub_navigation.dto.EventReportOverviewDTO;
import com.utem.event_hub_navigation.model.EventReport;
import com.utem.event_hub_navigation.model.ReportType;

public interface EventReportService {

    List<EventReport> getEventReport(Integer eventId);

    void storeReport(Integer eventId, ReportType reportType) throws IOException;


    EventReportOverviewDTO getEventReportOverviewDTO(Integer eventId);

    

}