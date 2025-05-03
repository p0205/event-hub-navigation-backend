package com.utem.event_hub_navigation.controller;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/events/{eventId}/attendance/{sessionId}")
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

    @PostMapping("/check_in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest checkInRequest) {
        try {
            String result = attendanceService.checkIn(checkInRequest.getQrCodePayload(),
                    checkInRequest.getParticipantId());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCheckInParticipants(@PathVariable("sessionId") Integer sessionId) {
        try {
            return ResponseEntity.ok(attendanceService.getCheckInParticipants(sessionId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

}
