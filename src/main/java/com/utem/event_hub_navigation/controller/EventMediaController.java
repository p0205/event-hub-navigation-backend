package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.service.impl.EventMediaService;

@RestController
@RequestMapping("/events/{eventId}/media")
public class EventMediaController {

    @Autowired
    private EventMediaService eventMediaService;

    @PostMapping
    public ResponseEntity<String> upload(
        @PathVariable("eventId") Integer eventId,
        @RequestParam("files") MultipartFile[] files
    ) {
        try {
            for(MultipartFile file: files){
                eventMediaService.uploadMedia(eventId, file);
            }
                
            return ResponseEntity.status(HttpStatus.CREATED).body("Upload successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMediaByEventId(@PathVariable("eventId") Integer eventId) {
        try {
            return ResponseEntity.ok(eventMediaService.getMediaByEventId(eventId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching media: " + e.getMessage());
        }
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<?> deleteEventMedia(@PathVariable("mediaId") Integer mediaId){

        try {
            eventMediaService.deleteEventMedia(mediaId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching media: " + e.getMessage());
        }
    }
}

