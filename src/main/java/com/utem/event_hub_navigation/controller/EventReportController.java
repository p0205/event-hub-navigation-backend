package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.EventArticleDTO;
import com.utem.event_hub_navigation.dto.ArticleManualInputsDto;
import com.utem.event_hub_navigation.model.ReportType;
import com.utem.event_hub_navigation.service.impl.EventReportServiceImpl;

@RequestMapping("/api/events/{eventId}/report")
@RestController
public class EventReportController {

    @Autowired
    private EventReportServiceImpl eventReportService;

    @GetMapping("/attendance/pdf")
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

    @GetMapping("/attendance")
    public ResponseEntity<?> getEventBudgetReport(@PathVariable Integer eventId,
            @RequestParam(value = "format", defaultValue = "pdf") String format) {
        try {
            eventReportService.storeReport(eventId, ReportType.ATTENDANCE);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    @GetMapping("/feedback")

    public ResponseEntity<byte[]> generateFeedbackReport(
            @PathVariable Integer eventId,
            @RequestParam(name = "commentsLimit", required = false) Integer commentsLimit) {
        try {
          
      
            byte[] pdfBytes = eventReportService.saveEventFeedbackReport(eventId, commentsLimit);

            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Event_Report_" + eventId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Catch runtime exceptions (e.g., "Event not found")
            System.err.println("Error processing feedback report for event ID " + eventId + ": " + e.getMessage());
            return new ResponseEntity<>(("Error: " + e.getMessage()).getBytes(), HttpStatus.NOT_FOUND); // Use NOT_FOUND
                                                                                                        // for event not
                                                                                                        // found
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(("An unexpected error occurred: " + e.getMessage()).getBytes(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getReportOverview(@PathVariable Integer eventId) {

        try {

            return ResponseEntity.ok(eventReportService.getEventReportOverviewDTO(eventId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    // @GetMapping("/article")
    // public ResponseEntity<String> getEventArticle(@PathVariable Integer eventId) {
    //     try {
    //         EventArticleDTO article = eventReportService.generateEventArticle(eventId);
    //         return ResponseEntity.ok(article.getMainArticleContent());
    //     } catch (RuntimeException e) {
    //         System.out.println(e.toString());
    //         return ResponseEntity.notFound().build(); // Or handle specific exceptions with @ExceptionHandler
    //     }
    // }

    @PostMapping("/article")
    public ResponseEntity<String> generateEventArticleWithManualInputs(
            @PathVariable Integer eventId,
            @RequestBody ArticleManualInputsDto inputsDto) {
        try {
            String article = eventReportService.generateEventArticle(
                eventId,
                inputsDto.getOrganizingBody(),
                inputsDto.getCreditIndividuals(),
                inputsDto.getEventObjectives(),
                inputsDto.getActivitiesConducted(),
                inputsDto.getTargetAudience(),
                inputsDto.getPerceivedImpact(),
                inputsDto.getAcknowledgements(),
                inputsDto.getAppreciationMessage(),
                inputsDto.getLanguage()
            );
            return ResponseEntity.ok(article);
        } catch (RuntimeException e) {
            System.err.println("Error generating article for event " + eventId + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
