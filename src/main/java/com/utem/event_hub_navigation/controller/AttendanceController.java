package com.utem.event_hub_navigation.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
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

import com.utem.event_hub_navigation.dto.CheckInRequest;
import com.utem.event_hub_navigation.service.AttendanceService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/events/{eventId}/attendance/{sessionId}")
public class AttendanceController {

    private AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // @PostMapping("/mark")
    // public ResponseEntity<String> markAttendance(@RequestBody AttendanceRequest
    // request) {
    // String response = attendanceService.markAttendance(request.getUserId(),
    // request.getQrData());
    // return ResponseEntity.ok(response);
    // }

    @PostMapping("/check_in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest checkInRequest) {
        try {
            System.out.println("Received check-in request: " + checkInRequest);
            String result = attendanceService.checkIn(checkInRequest.getQrCodePayload(),
                    checkInRequest.getEmail());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid check-in request: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error during check-in: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No participant found with the provided ID.");
        }
    }


    @GetMapping(value = "/qr_download", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable("eventId") Integer eventId,
            @PathVariable("sessionId") Integer sessionId) {
        try {

            byte[] image = attendanceService.downloadQRCode(sessionId); // Assuming you stored QR as byte[]

            if (image.equals(null)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("qrcode.png")
                    .build());

            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate a QR code with a custom expiration date.
     * 
     * @param sessionId The session ID.
     * @param eventId   The event ID.
     * @param expiresAt The expiration date-time in ISO 8601 format.
     * @return The PNG byte array for the QR code.
     */

    @GetMapping(value = "/qr_generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQRCode(
            @PathVariable("eventId") Integer eventId,
            @PathVariable("sessionId") Integer sessionId,
            @RequestParam(required = false) LocalDateTime expiresAt) {
        try {
            byte[] qrCode = attendanceService.generateAndSaveQRCode(eventId, sessionId, expiresAt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("session-" + sessionId + "-qr.png")
                    .build());

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

   

    @GetMapping
    public ResponseEntity<?> getCheckInParticipants(
            @PathVariable("sessionId") Integer sessionId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            return ResponseEntity.ok(attendanceService.getCheckInParticipants(sessionId, pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }


    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAttendanceXlsx( // Changed method name
    
            @PathVariable Integer sessionId,
            @RequestParam(required = false) String sessionName) { // sessionName is optional for filename

        try {
            // Generate the XLSX data using the service
            byte[] xlsxBytes = attendanceService.exportAttendanceXLSX(sessionId); // Call new XLSX service method

            // Determine the filename based on sessionName or default
            String filename;
            if (sessionName != null && !sessionName.trim().isEmpty()) {
                // Sanitize session name for filename (replace spaces with underscores, remove special chars)
                String sanitizedSessionName = sessionName.replaceAll("[^a-zA-Z0-9.-]", "_");
                filename = String.format("attendance-%s.xlsx", sanitizedSessionName); // Changed extension to .xlsx
            } else {
                filename = String.format("attendance-session-%d.xlsx", sessionId); // Changed extension to .xlsx
            }

            // Set HTTP Headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")); // Changed Content-Type for XLSX
            // Force download and specify filename
            headers.setContentDispositionFormData("attachment", filename);
            // Set content length for better download progress indication
            headers.setContentLength(xlsxBytes.length);

            // Return the XLSX bytes with appropriate headers
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(xlsxBytes);

        } catch (IOException e) {
            // Log the error and return an internal server error status
            System.err.println("Error generating XLSX for  session " + sessionId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            // Catch any other unexpected errors
            System.err.println("An unexpected error occurred during XLSX export for session " + sessionId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
