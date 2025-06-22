package com.utem.event_hub_navigation.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.itextpdf.text.DocumentException;
import com.utem.event_hub_navigation.dto.VenueUtilizationReportRequest;
import com.utem.event_hub_navigation.dto.DashboardDataDTO;
import com.utem.event_hub_navigation.dto.FilterDateRangeDTO;
import com.utem.event_hub_navigation.service.AdminService;
import com.utem.event_hub_navigation.service.impl.AdminReportServiceImpl;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminReportServiceImpl adminReportService;
  
    @Autowired
    public AdminController(AdminService adminService, AdminReportServiceImpl adminReportService) {
        this.adminService = adminService;
        this.adminReportService = adminReportService;
    }



    @PostMapping("/report/venue-utilization")
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

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDataDTO> getDashboardData(@RequestParam LocalDateTime startDateTime,@RequestParam LocalDateTime endDateTime) {

        System.out.println("Received request for dashboard data with startDateTime: " + startDateTime + " and endDateTime: " + endDateTime);
        DashboardDataDTO data = adminService.getDashboardData(startDateTime, endDateTime);
        System.out.println(data.toString());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/report/event-types-performance")
    public ResponseEntity<byte[]> generateEventTypesPerformanceReport(@RequestParam LocalDateTime startDateTime,@RequestParam LocalDateTime endDateTime ) {
        try {
            byte[] pdfBytes = adminReportService.generateEventTypesPerformanceReport(startDateTime,endDateTime);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "event-types-performance-report.pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (DocumentException | IOException e) {
            System.err.println("Error generating event types performance report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
