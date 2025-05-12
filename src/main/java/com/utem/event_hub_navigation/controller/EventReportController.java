package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.service.impl.EventReportServiceImpl;

@RequestMapping("/{eventId}/report")
@RestController
public class EventReportController {

    @Autowired
    private EventReportServiceImpl eventReportService;

    
    @GetMapping
    public ResponseEntity<?> getEventAttendanceReport(@PathVariable Integer eventId,
            @RequestParam(value = "format", defaultValue = "pdf") String format) {
        try {
            byte[] pdfBytes = eventReportService.generateEventAttendanceReport(eventId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Event_Report_" + eventId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }
}
