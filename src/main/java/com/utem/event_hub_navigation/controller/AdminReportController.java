package com.utem.event_hub_navigation.controller;

import java.io.IOException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.DocumentException;
import com.utem.event_hub_navigation.dto.VenueUtilizationReportRequest;
import com.utem.event_hub_navigation.service.impl.AdminReportServiceImpl;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

    private final AdminReportServiceImpl adminReportService;
  
    @Autowired
    public AdminReportController(AdminReportServiceImpl adminReportService) {
        this.adminReportService = adminReportService;
    }

    @PostMapping("/venue-utilization")
    public ResponseEntity<byte[]> generateVenueUtilizationReportPost(
            @RequestBody VenueUtilizationReportRequest request) {
               
        try {
            byte[] pdfBytes = adminReportService.generateVenueUtilizationReport(
                request.getStartDateTime(), 
                request.getEndDateTime(), 
                request.getVenueIds()
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "venue-utilization-report.pdf");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (DocumentException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
