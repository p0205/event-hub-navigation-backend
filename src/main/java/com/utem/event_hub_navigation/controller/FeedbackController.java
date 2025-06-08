package com.utem.event_hub_navigation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.FeedbackDTO;
import com.utem.event_hub_navigation.service.FeedbackService;

@RequestMapping("/api/event/{eventId}/feedback")
@RestController
public class FeedbackController {

    private FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService){
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<?> saveFeedback(@PathVariable("eventId") Integer eventId, @RequestBody FeedbackDTO feedback){
        try {
            feedbackService.saveFeedback(eventId, feedback);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> checkIfFeedbackExist(@PathVariable("eventId") Integer eventId, @RequestParam("userId") Integer userId){
        try {
            Boolean exist = feedbackService.checkIfFeedbackExist(eventId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("hasFeedback", exist);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
}
