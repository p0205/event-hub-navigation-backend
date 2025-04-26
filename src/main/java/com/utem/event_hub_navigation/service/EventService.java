package com.utem.event_hub_navigation.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import com.utem.event_hub_navigation.dto.EventResponseByStatus;
import com.utem.event_hub_navigation.dto.EventSimpleResponse;
import com.utem.event_hub_navigation.dto.EventVenueDTO;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.mapper.EventMapper;
import com.utem.event_hub_navigation.mapper.UserMapper;
import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventBudgetKey;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.EventVenue;
import com.utem.event_hub_navigation.model.EventVenueKey;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.utils.JsonParserUtil;
import com.utem.event_hub_navigation.utils.NumberParserUtil;

@Service
public class EventService {

    // CRUD
    private EventRepo eventRepo;

    private UserRepo userRepo;

    private EventMapper eventMapper;

    private UserMapper userMapper;

    private RegistrationRepo registrationRepo;

    @Autowired
    public EventService(EventRepo eventRepo, UserRepo userRepo, EventMapper eventMapper, UserMapper userMapper,
            RegistrationRepo registrationRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.eventMapper = eventMapper;
        this.userMapper = userMapper;
        this.registrationRepo = registrationRepo;
    }

    public EventDTO createEvent(EventDTO dto) {
        Event event = eventMapper.toEntity(dto);
        System.out.println("Event before saving: " + dto);

        System.out.println("Event before saving: " + event);
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
        return eventMapper.tDto(eventRepo.save(event));
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
            MultipartFile supportingDocument) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Integer organizerId = NumberParserUtil.parseInteger(organizerIdString, "organizer ID");
        Integer participantsNo = NumberParserUtil.parseInteger(participantsNoString, "participants number");

        if (participantsNo == null || participantsNo <= 0) {
            throw new IllegalArgumentException("Participants number must be a positive number.");
        }

        List<EventVenueDTO> venues = JsonParserUtil.parseJson(eventVenuesJson, new TypeReference<>() {
        });
        List<EventBudgetDTO> budgets = JsonParserUtil.parseJson(eventBudgetsJson, new TypeReference<>() {
        });

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
    // if (value == null || value.trim().isEmpty()) return null;
    // try {
    // return Integer.parseInt(value.trim());
    // } catch (NumberFormatException e) {
    // throw new IllegalArgumentException("Invalid number format for " + fieldName +
    // ": " + value);
    // }
    // }

    public String getEventName(Integer eventId) {
        return eventRepo.findNameById(eventId);
    }

    public List<EventDTO> getAllPendingEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.PENDING));
    }

    public List<EventDTO> getAllActiveEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.ACTIVE));
    }

    public List<EventDTO> getAllCompletedEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.COMPLETED));
    }

    public Event updateEvent(Integer eventId, EventDTO updatedEvent, Integer organizerId) {
        // Find the existing event
        Event existingEvent = eventRepo.findById(eventId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + eventId));

        // Fetch the new organizer User if organizerId is provided
        // User newOrganizer = null;
        // if (organizerId != null) {
        // newOrganizer = userRepository.findById(organizerId)
        // .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "New Organizer User
        // not found with ID: " + organizerId));
        // }

        // Update fields from eventDetails

        if (updatedEvent.getName() != null)
            existingEvent.setName(updatedEvent.getName());

        if (updatedEvent.getDescription() != null)
            existingEvent.setDescription(updatedEvent.getDescription());

        if (updatedEvent.getStartDateTime() != null)
            existingEvent.setStartDateTime(updatedEvent.getStartDateTime());

        if (updatedEvent.getEndDateTime() != null)
            existingEvent.setEndDateTime(updatedEvent.getEndDateTime());

        if (updatedEvent.getStatus() != null)
            existingEvent.setStatus(updatedEvent.getStatus());

        if (updatedEvent.getQrCodePath() != null)
            existingEvent.setQrCodePath(updatedEvent.getQrCodePath());

        if (updatedEvent.getSupportingDocument() != null)
            existingEvent.setSupportingDocument(updatedEvent.getSupportingDocument());

        if (updatedEvent.getParticipantsNo() != null)
            existingEvent.setParticipantsNo(updatedEvent.getParticipantsNo());

        // Update the organizer only if a new organizerId was provided
        // if (newOrganizer != null) {
        // existingEvent.setEventOrganizer(newOrganizer);
        // }
        // Note: createdAt should not be updated

        // Save the updated event
        return eventRepo.save(existingEvent);
    }

    public EventDTO getEventById(Integer id) {
        Optional<Event> optionalEvent = eventRepo.findById(id);

        return eventMapper.tDto(optionalEvent.get());

    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public List<Event> getEventsByOrganizer(Integer organizerId) {
        User organizer = userRepo.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organizer User not found with ID: " + organizerId));

        return eventRepo.findByOrganizerOrderByStartDateTimeDesc(organizer);
    }

    public EventResponseByStatus getEventsByOrganizerGroupedByStatus(Integer organizerId) {
        List<Event> allEvents = getEventsByOrganizer(organizerId);

        List<EventSimpleResponse> pendingEvents = new ArrayList<>();
        
        List<EventSimpleResponse> activeEvents = new ArrayList<>();
        List<EventSimpleResponse> completedEvents = new ArrayList<>();

        for (Event event : allEvents) {
            EventSimpleResponse eventSimpleResponse = EventSimpleResponse.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .startDateTime(event.getStartDateTime())
                    .status(event.getStatus())
                    .build();

            if (event.getStatus() == EventStatus.PENDING) {
                pendingEvents.add(eventSimpleResponse);
            } else if (event.getStatus() == EventStatus.ACTIVE) {
                activeEvents.add(eventSimpleResponse);
            } else if (event.getStatus() == EventStatus.COMPLETED) {
                completedEvents.add(eventSimpleResponse);
            }
        }

        return EventResponseByStatus.builder()
                .pendingEvents(pendingEvents)
                .activeEvents(activeEvents)
                .completedEvents(completedEvents)
                .build();
    }

    // public List<Event> getEventsByEventOrganizerAndStatus(Integer organizerId, EventStatus status, String sortBy) {
    //     User organizer = userRepo.findById(organizerId)
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
    //                     "Organizer User not found with ID: " + organizerId));

    //     return eventRepo.findByOrganizerAndStatus(organizer, status);
    // }

    // public List<Event> getEventsByDate(LocalDate date) {

    // return eventRepo.findByEventDate(date);
    // }

    public boolean deleteEvent(Integer id) {
        if (eventRepo.existsById(id)) {
            eventRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserDTO> getParticipantsInfoFromFile(MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> emails = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header row
                Cell emailCell = row.getCell(0); // Assumes email is in column 0
                if (emailCell != null) {
                    // emailCell.setCellType(CellType.STRING);
                    String email = emailCell.getStringCellValue().trim();
                    if (!email.isEmpty())
                        emails.add(email);
                }
            }

            // Query the database for users by email
            List<User> users = userRepo.findByEmailIn(emails);

            // Format response
            List<UserDTO> result = users.stream().map(user -> {
                UserDTO userdto = userMapper.toUserDTO(user);
                return userdto;
            }).collect(Collectors.toList());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing file", e);

        }
    }

    public List<UserDTO> importParticipants(Integer eventId, List<UserDTO> participants) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<UserDTO> results = new ArrayList<>();
        for (UserDTO dto : participants) {
            Optional<User> optionalUser = userRepo.findByEmail(dto.getEmail());

            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                continue;
                // Optional: Create new user if not exists
                // user = new User();
                // user.setEmail(dto.getEmail());
                // user.setName(dto.getName());
                // user.setFaculty(dto.getFaculty());
                // user.setCreatedAt(LocalDateTime.now());
                // user = userRepository.save(user);
                // isNewUser = true;
            }

            // Check if already registered
            boolean alreadyRegistered = registrationRepo.existsByEventAndParticipant(event, user);
            if (!alreadyRegistered) {
                Registration registration = Registration.builder()
                        .event(event)
                        .participant(user)
                        .checkinDateTime(null)
                        .register_date(LocalDate.now())
                        .build();

                registrationRepo.save(registration);
            }

            // Add result info (for optional UI summary)
            results.add(userMapper.toUserDTO(user));
        }

        return results;

    }
}
