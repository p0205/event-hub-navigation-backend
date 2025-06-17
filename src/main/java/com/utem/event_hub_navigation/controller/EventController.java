package com.utem.event_hub_navigation.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.dto.EventResponseByStatus;
import com.utem.event_hub_navigation.dto.EventSimpleResponse;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.EventType;
import com.utem.event_hub_navigation.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    // --- Use constructor injection for ObjectMapper and EventService ---
    public EventController(EventService eventService) {
        this.eventService = eventService;
        // You do NOT need to manually register the module here if using Spring Boot
        // auto-config
        // If NOT using Spring Boot or auto-config is disabled, you would:
        // this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Create a new event
    // POST /events?organizerId=...
    // The request body should contain event details *without* the full Users object,
    // but the organizerId is passed as a request parameter.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventDTO> createEvent(
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart("type") String type,
            @RequestPart("organizerId") String organizerIdString,
            @RequestPart("participantsNo") String participantsNoString,
            @RequestPart("sessions") String sessionsJson,
            @RequestPart("eventBudgets") String eventBudgetsJson,
            @RequestPart(value = "supportingDocument", required = false) MultipartFile supportingDocument) {
        EventDTO dto = eventService.prepareAndValidateEvent(
                name, description, organizerIdString,
                participantsNoString, sessionsJson, eventBudgetsJson, supportingDocument,type);
                // dto.setType(type);
System.out.println("create new event");
        EventDTO savedEvent = eventService.createEvent(dto);
        System.out.println("create new event end");
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }

    // GET /events
    // @GetMapping
    // public ResponseEntity<List<Event>> getAllEvents() {
    // List<Event> events = eventService.getAllEvents();
    // // Note: If you need organizer details here, ensure your service/repository
    // // uses FETCH JOIN or consider a DTO to avoid fetching large amounts of data.
    // return ResponseEntity.ok(events);
    // }

    @GetMapping("/pending")
    public ResponseEntity<List<EventDTO>> getAllPendingEvents() {
        List<EventDTO> events = eventService.getAllPendingEvents();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/active")
    public ResponseEntity<List<EventDTO>> getAllActiveEvents() {
        List<EventDTO> events = eventService.getAllActiveEvents();
        // Note: If you need organizer details here, ensure your service/repository
        // uses FETCH JOIN or consider a DTO to avoid fetching large amounts of data.
        return ResponseEntity.ok(events);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<EventDTO>> getAllCompletedEvents() {
        List<EventDTO> events = eventService.getAllCompletedEvents();
        // Note: If you need organizer details here, ensure your service/repository
        // uses FETCH JOIN or consider a DTO to avoid fetching large amounts of data.
        return ResponseEntity.ok(events);
    }

    // Get event by ID
    // GET /events/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Integer id) {
        // The fetched event will include the associated organizer (potentially lazily
        // loaded)
        EventDTO event = eventService.getEventDTOById(id);
        if (event != null)
            return ResponseEntity.ok(event);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // GET /events/{id/name}
    @GetMapping("{id}/name")
    public ResponseEntity<String> getEventNameById(@PathVariable Integer id) {
        // The fetched event will include the associated organizer (potentially lazily
        // loaded)
        String eventName = eventService.getEventName(id);
       
        if (eventName != null)
            return ResponseEntity.ok(eventName);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // Update an existing event
    // PATCH /events/{id}... (organizerId is optional if not changing organizer)
    // Request body contains updated event details (excluding the complex Users
    // object)
    @PatchMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Integer id,
            @RequestBody EventDTO eventDetails,
            @RequestParam(required = false) Integer organizerId) { // organizerId is optional

        // Service handles updating the event and potentially changing the organizer
        Event updatedEvent = eventService.updateEvent(id, eventDetails, organizerId);
        return ResponseEntity.ok(updatedEvent);
    }

    // Delete an event by ID
    // DELETE /events/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        boolean isDeleted = eventService.deleteEvent(id);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + id);
        }
    }

    // --- Custom Query Endpoints ---

    // Get events by organizer Users ID and status
    // GET /api/events/search?organizerId=...&status=...
    @GetMapping("/byOrganizerAndStatus")
    public ResponseEntity<List<EventSimpleResponse>> getEventsByOrganizerAndStatus(
            @RequestParam Integer organizerId, // Accept organizer ID
            @RequestParam EventStatus status) {

        List<EventSimpleResponse> events = eventService.getEventsByEventOrganizerAndStatus(organizerId, status);
        return ResponseEntity.ok(events);
    }

    // Get events by date
    // GET /events/byDate?date=YYYY-MM-DD
    // @GetMapping("/byDate")
    // public ResponseEntity<List<Event>> getEventsByDate(
    // @RequestParam LocalDate date) {

    // List<Event> events = eventService.getEventsByDate(date);
    // return ResponseEntity.ok(events);
    // }

    // Get events by organizer Users ID
    // GET /events/byOrganizer?organizerId=...
    @GetMapping("/byOrganizer")
    public ResponseEntity<EventResponseByStatus> getEventsByOrganizer(
            @RequestParam Integer organizerId) {

        EventResponseByStatus events = eventService.getEventsByOrganizerGroupedByStatus(organizerId);
        return ResponseEntity.ok(events);
    }

    // Get sessions of an event
    @GetMapping("/{eventId}/sessions")
    public ResponseEntity<?> getSessionsByEvent(@PathVariable("eventId") Integer eventId) {
        try {
            return ResponseEntity.ok(eventService.getSessionsByEvent(eventId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Import participants to get information but no insert to database yet
    @PostMapping("/{eventId}/participants/getInfo")
    public ResponseEntity<List<UserDTO>> importParticipants(
            // @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {

        List<UserDTO> participantList = eventService.getParticipantsInfoFromFile(file);
        return ResponseEntity.ok(participantList);
    }

    // Add participants to event
    @PostMapping("/{eventId}/participants")
    public ResponseEntity<List<UserDTO>> importParticipants(@PathVariable("eventId") Integer eventId,
            @RequestBody List<UserDTO> participantList) {

        List<UserDTO> registeredList = eventService.importParticipants(eventId, participantList);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredList);
    }

    // Get participants by event
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Integer eventId,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "participant.name") String sortBy) {
        try {
            Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy));
            Page<UserDTO> participants = eventService.getParticipantsByEventId(eventId, paging);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get participants by event
    @GetMapping("/{eventId}/participants/demographics")
    public ResponseEntity<?> getParticipantsDemographics(@PathVariable Integer eventId) {
        try {
            return ResponseEntity.ok(eventService.getParticipantsDemographicsByEventId(eventId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Delete Participants
    @DeleteMapping("/{eventId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Integer eventId,
            @PathVariable Integer participantId) {
        boolean isRemoved = eventService.removeParticipant(eventId, participantId);
        if (isRemoved) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Participant not found with ID: " + participantId + " in event ID: " + eventId);
        }
    }

    // MOBILE APP APIs
    // Get calender events to be displayed
    @GetMapping("/calendar/all-events")
    public ResponseEntity<?> getAllEventsByMonth( @RequestParam("startDateTime") LocalDateTime startDateTime,
    @RequestParam("endDateTime") LocalDateTime endDateTime) {
        try {
            return ResponseEntity.ok(eventService.getAllCalendarEventByMonth(startDateTime,endDateTime));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
    
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalenderEvent(@RequestParam("userId") Integer userId) {
        try {
            return ResponseEntity.ok(eventService.getCalendarEvent(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    //Get Participants' Upcoming Events
    @GetMapping("/participant/upcoming-events")
    public ResponseEntity<?> getParticipantUpcomingEvents(@RequestParam("userId") Integer userId) {
        try {
            return ResponseEntity.ok(eventService.getParticipantsUpcomingEvents(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
    //Get Participants' Past Events
    @GetMapping("/participant/past-events")
    public ResponseEntity<?> getParticipantPastEvents(@RequestParam("userId") Integer userId) {
        try {
            return ResponseEntity.ok(eventService.getParticipantsPastEvents(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
    //Get Participants' Past Events
    @GetMapping("/participant/calendar-events")
    public ResponseEntity<?> getParticipantEventsByMonth(
            @RequestParam("userId") Integer userId,
            @RequestParam("startDateTime") LocalDateTime startDateTime,
            @RequestParam("endDateTime") LocalDateTime endDateTime) {
        try {
            return ResponseEntity.ok(eventService.getParticipantsEventsByMonth(userId, startDateTime, endDateTime));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }

    //Get Event Details
    @GetMapping("/{eventId}/details")
    public ResponseEntity<?> getEventDetails(@PathVariable("eventId") Integer eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventDetails(eventId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error: " + e.getMessage());
        }
    }
    

}
