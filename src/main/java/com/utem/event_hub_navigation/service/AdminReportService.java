package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.itextpdf.text.DocumentException;

public interface AdminReportService {

    public byte[] generateVenueUtilizationReport(LocalDateTime startDateTime, LocalDateTime endDateTime,
            List<Integer> venueIds) throws DocumentException, IOException;

   
}
