package com.utem.event_hub_navigation.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.model.EventMedia;
import com.utem.event_hub_navigation.repo.EventMediaRepo;
import com.utem.event_hub_navigation.utils.SupabaseStorageService;

@Service
public class EventMediaService {

    @Autowired
    private EventMediaRepo eventMediaRepo;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    public void uploadMedia(Integer eventId, MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = supabaseStorageService.uploadFile(file, "event-media", filename);

        EventMedia media =  EventMedia.builder()
                            .eventId(eventId)
                            .filename(filename)
                            .fileUrl(fileUrl)
                            .uploadedAt(LocalDateTime.now())
                            .build();
      
        eventMediaRepo.save(media);
    }

    public List<EventMedia> getMediaByEventId(Integer eventId) {
        return eventMediaRepo.findByEventId(eventId);
    }

    public void deleteEventMedia(Integer mediaId){
        eventMediaRepo.deleteById(mediaId);
    }
}

