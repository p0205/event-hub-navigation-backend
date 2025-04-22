package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.dto.EventVenueDTO;
import com.utem.event_hub_navigation.mapper.EventMapper;
import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventBudgetKey;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.EventVenue;
import com.utem.event_hub_navigation.model.EventVenueKey;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.utils.JsonParserUtil;
import com.utem.event_hub_navigation.utils.NumberParserUtil;

@Service
public class EventService {

    //CRUD
    private EventRepo eventRepo;

    private UserRepo userRepo;


    private EventMapper eventMapper;

    @Autowired
    public EventService(EventRepo eventRepo, UserRepo userRepo, EventMapper eventMapper) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.eventMapper = eventMapper;
    }


     public Event createEvent(EventDTO dto) {
        Event event = eventMapper.toEntity(dto);

        // // Manually set back-reference
        // event.getEventVenues().forEach(ev -> ev.setEvent(event));
        // event.getEventBudgets().forEach(eb -> eb.setEvent(event));
    
        for (EventBudget budget : event.getEventBudgets()) {
            budget.setEvent(event);
            EventBudgetKey key = new EventBudgetKey();
            key.setEventId(event.getId()); // must not be null at this point
            key.setBudgetId(budget.getBudgetCategory().getId());
            budget.setId(key);
        }
        for (EventVenue venue : event.getEventVenues()) {
            venue.setEvent(event);
            EventVenueKey key = new EventVenueKey();
            key.setEventId(event.getId()); // must not be null at this point
            key.setVenueId(venue.getVenue().getId());
            venue.setId(key);
        }
        return eventRepo.save(event);
    }

    public EventDTO prepareAndValidateEvent(
        String name,
        String description,
        String organizerIdString,
        String startDateTime,
        String endDateTime,
        String participantsNoString,
        String eventVenuesJson,
        String eventBudgetsJson,
        MultipartFile supportingDocument
) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    Integer organizerId = NumberParserUtil.parseInteger(organizerIdString, "organizer ID");
    Integer participantsNo = NumberParserUtil.parseInteger(participantsNoString, "participants number");

    if (participantsNo == null || participantsNo <= 0) {
        throw new IllegalArgumentException("Participants number must be a positive number.");
    }

    List<EventVenueDTO> venues = JsonParserUtil.parseJson(eventVenuesJson, new TypeReference<>() {});
    List<EventBudgetDTO> budgets = JsonParserUtil.parseJson(eventBudgetsJson, new TypeReference<>() {});

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);

    EventDTO dto = new EventDTO();
    dto.setName(name);
    dto.setDescription(description);
    dto.setOrganizerId(organizerId);
    dto.setStartDateTime(start);
    dto.setEndDateTime(end);
    dto.setParticipantsNo(participantsNo);
    dto.setEventVenues(venues);
    dto.setEventBudgets(budgets);

    if (supportingDocument != null && !supportingDocument.isEmpty()) {
        try {
            Document doc = Document.builder()
                    .filename(supportingDocument.getOriginalFilename())
                    .fileType(supportingDocument.getContentType())
                    .data(supportingDocument.getBytes())
                    .build();
            dto.setSupportingDocument(doc);
        } catch (IOException e) {
            throw new RuntimeException("Error reading uploaded file", e);
        }
    }

    dto.setStatus(EventStatus.PENDING);
    return dto;
}

// private Integer parseInteger(String value, String fieldName) {
//     if (value == null || value.trim().isEmpty()) return null;
//     try {
//         return Integer.parseInt(value.trim());
//     } catch (NumberFormatException e) {
//         throw new IllegalArgumentException("Invalid number format for " + fieldName + ": " + value);
//     }
// }


    public Event updateEvent(Integer eventId, EventDTO updatedEvent, Integer organizerId) {
        // Find the existing event
        Event existingEvent = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + eventId));

        // Fetch the new organizer User if organizerId is provided
        // User newOrganizer = null;
        // if (organizerId != null) {
        //      newOrganizer = userRepository.findById(organizerId)
        //             .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "New Organizer User not found with ID: " + organizerId));
        // }


        // Update fields from eventDetails

        if(updatedEvent.getName() != null) existingEvent.setName(updatedEvent.getName());

        if(updatedEvent.getDescription() != null) existingEvent.setDescription(updatedEvent.getDescription());
        
        if(updatedEvent.getStartDateTime() != null) existingEvent.setStartDateTime(updatedEvent.getStartDateTime());
        
        if(updatedEvent.getEndDateTime() != null) existingEvent.setEndDateTime(updatedEvent.getEndDateTime());
        
        if(updatedEvent.getStatus() != null) existingEvent.setStatus(updatedEvent.getStatus());
        
        if(updatedEvent.getQrCodePath() != null) existingEvent.setQrCodePath(updatedEvent.getQrCodePath());
        
        if(updatedEvent.getSupportingDocument() != null) existingEvent.setSupportingDocument(updatedEvent.getSupportingDocument());
        
        if(updatedEvent.getParticipantsNo() != null) existingEvent.setParticipantsNo(updatedEvent.getParticipantsNo());
        
        

        // Update the organizer only if a new organizerId was provided
        // if (newOrganizer != null) {
        //      existingEvent.setEventOrganizer(newOrganizer);
        // }
        // Note: createdAt should not be updated

        // Save the updated event
        return eventRepo.save(existingEvent);
    }

    public Optional<Event> getEventById(Integer id) {
        return eventRepo.findById(id);
    }


    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }
   

    

    public List<Event> getEventsByOrganizer(Integer organizerId) {
        User organizer = userRepo.findById(organizerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer User not found with ID: " + organizerId));

        return eventRepo.findByOrganizer(organizer);
    }
    public List<Event> getEventsByEventOrganizerAndStatus(Integer organizerId, EventStatus status) {
        User organizer = userRepo.findById(organizerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer User not found with ID: " + organizerId));

        return eventRepo.findByOrganizerAndStatus(organizer,status);
    }

    // public List<Event> getEventsByDate(LocalDate date) {

    //     return eventRepo.findByEventDate(date);
    // }


    public boolean deleteEvent(Integer id) {
        if (eventRepo.existsById(id)) {
            eventRepo.deleteById(id);
            return true;
        }
        return false;
    }

    


}
