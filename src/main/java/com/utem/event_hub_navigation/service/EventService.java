package com.utem.event_hub_navigation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.dto.CalendarEventDTO;
import com.utem.event_hub_navigation.dto.EventDTO;
import com.utem.event_hub_navigation.dto.EventResponseByStatus;
import com.utem.event_hub_navigation.dto.EventSimpleResponse;
import com.utem.event_hub_navigation.dto.ParticipantsDemographicsDTO;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.Session;

public interface EventService {

    EventDTO createEvent(EventDTO dto);

    EventDTO prepareAndValidateEvent(
            String name,
            String description,
            String organizerIdString,
            String participantsNoString,
            String sessionsJson,
            String eventBudgetsJson,
            MultipartFile supportingDocument);

    String getEventName(Integer eventId);

    List<EventDTO> getAllPendingEvents();

    List<EventDTO> getAllActiveEvents();

    List<EventDTO> getAllCompletedEvents();

    Event updateEvent(Integer eventId, EventDTO updatedEvent, Integer organizerId);

    EventDTO getEventDTOById(Integer id);

    Event getEventById(Integer id);

    List<Event> getAllEvents();

    List<Event> getEventsByOrganizer(Integer organizerId);

    EventResponseByStatus getEventsByOrganizerGroupedByStatus(Integer organizerId);

    List<EventSimpleResponse> getEventsByEventOrganizerAndStatus(Integer organizerId, EventStatus status);

    boolean deleteEvent(Integer id);

    List<Session> getSessionsByEvent(Integer eventId);

    List<UserDTO> getParticipantsInfoFromFile(MultipartFile file);

    List<UserDTO> importParticipants(Integer eventId, List<UserDTO> participants);

    Page<UserDTO> getParticipantsByEventId(Integer eventId, Pageable pageable);

    ParticipantsDemographicsDTO getParticipantsDemographicsByEventId(Integer eventId);

    boolean removeParticipant(Integer eventId, Integer participantId);

    List<CalendarEventDTO> getCalendarEvent(Integer userID) throws Exception;

    List<Event> getOverdueActiveEvents();

    void markEventsAsCompleted(Event event);

}