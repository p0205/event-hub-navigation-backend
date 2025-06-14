package com.utem.event_hub_navigation.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.utem.event_hub_navigation.dto.CalendarEventDTO;
import com.utem.event_hub_navigation.dto.EventBudgetDTO;
import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.dto.EventResponseByStatus;
import com.utem.event_hub_navigation.dto.EventSimpleResponse;
import com.utem.event_hub_navigation.dto.ParticipantEventDetails;
import com.utem.event_hub_navigation.dto.ParticipantEventDetailsSessionDTO;
import com.utem.event_hub_navigation.dto.ParticipantEventDetailsVenueDTO;
import com.utem.event_hub_navigation.dto.ParticipantEventOverviewResponse;
import com.utem.event_hub_navigation.dto.ParticipantsDemographicsDTO;
import com.utem.event_hub_navigation.dto.SessionDTO;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.mapper.EventMapper;
import com.utem.event_hub_navigation.mapper.UserMapper;
import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventBudget;
import com.utem.event_hub_navigation.model.EventBudgetKey;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.Session;
import com.utem.event_hub_navigation.model.SessionVenue;
import com.utem.event_hub_navigation.model.SessionVenueKey;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.repo.EventRepo;
import com.utem.event_hub_navigation.repo.SessionRepo;
import com.utem.event_hub_navigation.repo.RegistrationRepo;
import com.utem.event_hub_navigation.repo.UserRepo;
import com.utem.event_hub_navigation.service.BudgetCategoryService;
import com.utem.event_hub_navigation.service.EventService;
import com.utem.event_hub_navigation.service.VenueService;
import com.utem.event_hub_navigation.utils.DataConversionUtil;
import com.utem.event_hub_navigation.utils.JsonParserUtil;
import com.utem.event_hub_navigation.utils.NumberParserUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EventServiceImpl implements EventService {

    // CRUD
    private EventRepo eventRepo;

    private UserRepo userRepo;

    private EventMapper eventMapper;

    private UserMapper userMapper;

    private RegistrationRepo registrationRepo;

    private SessionRepo sessionRepo;

    private VenueService venueService;

    private BudgetCategoryService budgetCategoryService;

    @Autowired
    public EventServiceImpl(EventRepo eventRepo, UserRepo userRepo, EventMapper eventMapper, UserMapper userMapper,
            RegistrationRepo registrationRepo, SessionRepo sessionRepo, VenueService venueService,
            BudgetCategoryService budgetCategoryService) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.eventMapper = eventMapper;
        this.userMapper = userMapper;
        this.registrationRepo = registrationRepo;
        this.sessionRepo = sessionRepo;
        this.venueService = venueService;
        this.budgetCategoryService = budgetCategoryService;
    }

    @Override
    @Transactional
    public EventDTO createEvent(EventDTO dto) {

        Event event = eventMapper.toEntity(dto);
        // 1. Set back-references
        List<Session> sessions = new ArrayList<>();
        for (Session session : event.getSessions()) {
            session.setEvent(event);

            // 2. Manually build sessionVenues from DTO
            SessionDTO sessionDTO = dto.getSessions().stream()
                    .filter(s -> s.getSessionName().equals(session.getSessionName())) // assuming session name is unique
                                                                                      // here
                    .findFirst()
                    .orElseThrow();

            List<SessionVenue> sessionVenues = new ArrayList<>();
            for (Venue venue : sessionDTO.getVenues()) {

                Venue managedVenue = venueService.getVenue(venue.getId());
                if (managedVenue.equals(null))
                    throw new EntityNotFoundException("Venue not found with ID: " + venue.getId());

                SessionVenueKey key = new SessionVenueKey();
                key.setSessionId(session.getId()); // will be null now, updated after save
                key.setVenueId(venue.getId());

                SessionVenue sv = new SessionVenue();
                sv.setVenue(managedVenue);
                sv.setSession(session);
                sv.setId(key);

                sessionVenues.add(sv);
            }

            session.setSessionVenues(sessionVenues);
            sessions.add(session);
        }

        event.setSessions(sessions);

        // Find the earliest start date/time among all sessions
        Optional<LocalDateTime> earliestStartTime = sessions.stream()
                .map(Session::getStartDateTime)
                .filter(dt -> dt != null) // Filter out any potential null dates
                .min(Comparator.naturalOrder());

        // Find the latest end date/time among all sessions
        Optional<LocalDateTime> latestEndTime = sessions.stream()
                .map(Session::getEndDateTime)
                .filter(dt -> dt != null) // Filter out any potential null dates
                .max(Comparator.naturalOrder());

        // Set the calculated dates on the event
        earliestStartTime.ifPresent(event::setStartDateTime);
        latestEndTime.ifPresent(event::setEndDateTime);

        // 3. Handle budgets
        for (EventBudget budget : event.getEventBudgets()) {
            System.out.println(budget.toString());

            EventBudgetKey key = new EventBudgetKey();
            key.setBudgetId(budget.getId().getBudgetId());
            key.setEventId(event.getId());

            BudgetCategory category = budgetCategoryService.getBudgetCategoryById(budget.getId().getBudgetId());

            // key.setEventId() will be set after save
            budget.setId(key);
            budget.setEvent(event);
            budget.setBudgetCategory(category);
            // System.out.println("BUDGET T:" +budget.toString());
        }
        return eventMapper.toDto(eventRepo.save(event)); // save again if budget ID needed
    }

    @Override
    public EventDTO prepareAndValidateEvent(
            String name,
            String description,
            String organizerIdString,
            String participantsNoString,
            String sessionsJson,
            String eventBudgetsJson,
            MultipartFile supportingDocument) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Integer organizerId = NumberParserUtil.parseInteger(organizerIdString, "organizer ID");
        Integer participantsNo = NumberParserUtil.parseInteger(participantsNoString, "participants number");

        if (participantsNo == null || participantsNo <= 0) {
            throw new IllegalArgumentException("Participants number must be a positive number.");
        }

        List<SessionDTO> venues = JsonParserUtil.parseJson(sessionsJson, new TypeReference<>() {
        });
        System.out.println(sessionsJson.toString());
        System.out.println(venues.toString());
        List<EventBudgetDTO> budgets = JsonParserUtil.parseJson(eventBudgetsJson, new TypeReference<>() {
        });

        EventDTO dto = new EventDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setOrganizerId(organizerId);
        dto.setParticipantsNo(participantsNo);
        dto.setSessions(venues);
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

    @Override
    public String getEventName(Integer eventId) {
        return eventRepo.findNameById(eventId);
    }

    @Override
    public List<EventDTO> getAllPendingEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.PENDING));
    }

    @Override
    public List<EventDTO> getAllActiveEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.ACTIVE));
    }

    @Override
    public List<EventDTO> getAllCompletedEvents() {
        return eventMapper.toEventDTOs(eventRepo.findByStatus(EventStatus.COMPLETED));
    }

    @Override
    public Event updateEvent(Integer eventId, EventDTO updatedEvent, Integer organizerId) {
        // Find the existing event
        Event existingEvent = eventRepo.findById(eventId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with ID: " + eventId));

        // Fetch the new organizer Users if organizerId is provided
        // Users newOrganizer = null;
        // if (organizerId != null) {
        // newOrganizer = userRepository.findById(organizerId)
        // .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "New Organizer
        // Users
        // not found with ID: " + organizerId));
        // }

        // Update fields from eventDetails

        if (updatedEvent.getName() != null)
            existingEvent.setName(updatedEvent.getName());

        if (updatedEvent.getDescription() != null)
            existingEvent.setDescription(updatedEvent.getDescription());

        if (updatedEvent.getStatus() != null)
            existingEvent.setStatus(updatedEvent.getStatus());

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

    @Override
    public EventDTO getEventDTOById(Integer id) {
        Optional<Event> optionalEvent = eventRepo.findById(id);
        
        return eventMapper.toDto(optionalEvent.get());

    }

    @Override
    public Event getEventById(Integer id) {
        Optional<Event> optionalEvent = eventRepo.findById(id);

        if (!optionalEvent.isPresent()) {
            return null;
        }
        return optionalEvent.get();

    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    @Override
    public List<Event> getEventsByOrganizer(Integer organizerId) {
        User organizer = userRepo.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organizer Users not found with ID: " + organizerId));

        return eventRepo.findByOrganizerOrderByStartDateTimeDesc(organizer);
    }

    @Override
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
                    .createdAt(event.getCreatedAt())
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

    @Override
    public List<EventSimpleResponse> getEventsByEventOrganizerAndStatus(Integer organizerId, EventStatus status) {
        User organizer = userRepo.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organizer Users not found with ID: " + organizerId));

        List<Event> events = eventRepo.findByOrganizerAndStatusOrderByStartDateTimeAsc(organizer, status);
        List<EventSimpleResponse> eventSimpleResponses = new ArrayList<>();
        for (Event event : events) {
            EventSimpleResponse eventSimpleResponse = EventSimpleResponse.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .startDateTime(event.getStartDateTime())
                    .status(event.getStatus())
                    .createdAt(event.getCreatedAt())
                    .build();
            eventSimpleResponses.add(eventSimpleResponse);
        }
        return eventSimpleResponses;
    }

    // public List<Event> getEventsByDate(LocalDate date) {

    // return eventRepo.findByEventDate(date);
    // }

    @Override
    public boolean deleteEvent(Integer id) {
        if (eventRepo.existsById(id)) {
            eventRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Session> getSessionsByEvent(Integer eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return sessionRepo.findByEvent(event);
    }

    @Override
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

    @Override
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
                // user = new Users();
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

    @Override
    public Page<UserDTO> getParticipantsByEventId(Integer eventId, Pageable pageable) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Page<Registration> registrations = registrationRepo.findByEvent(event, pageable);

        return registrations.map(registration -> userMapper.toUserDTO(registration.getParticipant()));

    }

    @Override
    public ParticipantsDemographicsDTO getParticipantsDemographicsByEventId(Integer eventId) {
        // Event event = eventRepo.findById(eventId)
        // .orElseThrow(() -> new RuntimeException("Event not found"));

        int totalNumber = registrationRepo.countByEventId(eventId);
        Map<String, Long> facultyData = DataConversionUtil.convertObjectListToMap(
                registrationRepo.getDemographicDataGroupByFaculty(eventId));

        Map<String, Long> courseData = DataConversionUtil
                .convertObjectListToMap(registrationRepo.getDemographicDataGroupByCourse(eventId));

        Map<String, Long> yearData = DataConversionUtil
                .convertObjectListToMap(registrationRepo.getDemographicDataGroupByYear(eventId));

        Map<String, Long> genderData = DataConversionUtil
                .convertObjectListToMap(registrationRepo.getDemographicDataGroupByGender(eventId));

        ParticipantsDemographicsDTO demographicsDTO = ParticipantsDemographicsDTO.builder()
                .totalNumber(totalNumber)
                .byCourse(courseData)
                .byFaculty(facultyData)
                .byGender(genderData)
                .byYear(yearData)
                .build();

        return demographicsDTO;

    }

    @Override
    public boolean removeParticipant(Integer eventId, Integer participantId) {

        registrationRepo.deleteByUserIdUserId(eventId, participantId);
        return true;
    }

    @Override
    public List<Event> getOverdueActiveEvents() {
        return eventRepo.findByEndDateTimeBeforeAndStatus(LocalDateTime.now(), EventStatus.ACTIVE);
    }

    @Override
    public void markEventsAsCompleted(Event event) {
        event.setStatus(EventStatus.COMPLETED);
        eventRepo.save(event);
    }

    // MOBILE APP APIs
    // Get calendar event data
    @Override
    public List<CalendarEventDTO> getAllCalendarEventByMonth(LocalDateTime startDateTime,
            LocalDateTime endDateTime) throws Exception {
        try {
            return eventRepo.fetchAllCalendarEventByMonth(startDateTime, endDateTime);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<CalendarEventDTO> getCalendarEvent(Integer userID) throws Exception {
        try {
            return eventRepo.findCalendarEntriesByOrganizerId(userID);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<CalendarEventDTO> getParticipantsEventsByMonth(Integer userID,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) throws Exception {
        try {

            List<CalendarEventDTO> response = registrationRepo.fetchParticipantEventsByDateRange(userID,
                    startDateTime, endDateTime);
            System.out.println(startDateTime);
            System.out.println(endDateTime);

            System.out.println(response);
            return response;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // Get participant's upcoming events
    @Override
    public List<ParticipantEventOverviewResponse> getParticipantsUpcomingEvents(Integer userID) throws Exception {
        try {
            return registrationRepo.fetchParticipantUpcomingEvents(userID);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // Get participant's past events
    @Override
    public List<ParticipantEventOverviewResponse> getParticipantsPastEvents(Integer userID) throws Exception {
        try {
            return registrationRepo.fetchParticipantPastEvents(userID);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // @Override
    // public List<ParticipantEventOverviewResponse>
    // getParticipantsEventsByMonth(Integer userID, LocalDateTime startDateTime,
    // LocalDateTime endDateTime) throws Exception {
    // try {

    // List<ParticipantEventOverviewResponse> response =
    // registrationRepo.fetchParticipantEventsByDateRange(userID, startDateTime,
    // endDateTime);
    // System.out.println(response.size());
    // return response;
    // } catch (Exception e) {
    // throw new Exception(e.getMessage());
    // }
    // }

    @Override
    public ParticipantEventDetails getEventDetails(Integer eventId) throws Exception {

        try {
            List<Object[]> rows = eventRepo.findEventDetailsById(eventId);
            ParticipantEventDetails event = new ParticipantEventDetails();
            Map<Integer, ParticipantEventDetailsSessionDTO> sessionMap = new HashMap<>();

            for (Object[] row : rows) {
                if (event.getId() == null) {
                    event.setId((Integer) row[0]);
                    event.setEventName((String) row[1]);
                    event.setDescription((String) row[2]);

                    java.sql.Date sqlDate = (java.sql.Date) row[3];
                    event.setRegisterDate(sqlDate != null ? sqlDate.toLocalDate() : null);

                    java.sql.Timestamp startTimestamp = (java.sql.Timestamp) row[4];
                    event.setStartDateTime(startTimestamp != null ? startTimestamp.toLocalDateTime() : null);

                    java.sql.Timestamp endTimestamp = (java.sql.Timestamp) row[5];
                    event.setEndDateTime(endTimestamp != null ? endTimestamp.toLocalDateTime() : null);

                    event.setOrganizerName((String) row[6]);
                    event.setPicName((String) row[7]);
                    event.setPicContact((String) row[8]);
                }

                Integer sessionId = (Integer) row[9];
                if (sessionId != null) {
                    ParticipantEventDetailsSessionDTO session = sessionMap.get(sessionId);
                    if (session == null) {
                        session = new ParticipantEventDetailsSessionDTO();
                        session.setId(sessionId);
                        session.setSessionName((String) row[10]);

                        java.sql.Timestamp sessionStartTimestamp = (java.sql.Timestamp) row[11];
                        session.setStartDateTime(
                                sessionStartTimestamp != null ? sessionStartTimestamp.toLocalDateTime() : null);

                        java.sql.Timestamp sessionEndTimestamp = (java.sql.Timestamp) row[12];
                        session.setEndDateTime(
                                sessionEndTimestamp != null ? sessionEndTimestamp.toLocalDateTime() : null);

                        sessionMap.put(sessionId, session);
                    }

                    Integer venueId = (Integer) row[13];
                    if (venueId != null) {
                        ParticipantEventDetailsVenueDTO venue = new ParticipantEventDetailsVenueDTO();
                        venue.setId(venueId);
                        venue.setName((String) row[14]);

                        // Check if this venue is already added
                        boolean venueExists = session.getVenues().stream()
                                .anyMatch(v -> v.getId().equals(venueId));
                        if (!venueExists) {
                            session.getVenues().add(venue);
                        }
                    }
                }
            }

            event.setSessions(new ArrayList<>(sessionMap.values()));
            return event;

        } catch (Exception e) {
            throw new Exception("Error fetching event details: " + e.getMessage(), e);
        }
    }
}