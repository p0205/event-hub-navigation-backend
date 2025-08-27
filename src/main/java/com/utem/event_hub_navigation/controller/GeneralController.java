package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.CheckInRequest;
import com.utem.event_hub_navigation.service.AttendanceService;

@RestController
@RequestMapping("/api")
public class GeneralController {
    


    private AttendanceService attendanceService;

    @Autowired
    public GeneralController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest checkInRequest) {
        try {
            System.out.println("Received general check-in request: " + checkInRequest);

            String result = attendanceService.checkInById(checkInRequest);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error during check-in: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No participant found with the provided ID.");
        }
    }
}
