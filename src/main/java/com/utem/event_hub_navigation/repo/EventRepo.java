package com.utem.event_hub_navigation.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.dto.CalendarEventDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.Users;

@Repository
public interface EventRepo extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizerAndStatusOrderByStartDateTimeAsc(Users organizer, EventStatus status);

    // List<Event> findByOrganizerAndStatusOrderByStartDateTimeDesc(Users organizer,
    // EventStatus status);

    // @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startOfDay AND
    // e.startDateTime < :endOfDay")
    // List<Event> findByEventDate(@Param("startOfDay") LocalDateTime startOfDay,
    // @Param("endOfDay") LocalDateTime endOfDay);

    // You might also want to find events by just the organizer user

    List<Event> findByOrganizerOrderByStartDateTimeDesc(Users organizer);

    List<Event> findByStatus(EventStatus status);

    @Query("SELECT e.name FROM Event e WHERE e.id = :id")
    String findNameById(@Param("id") Integer id);

    @Query(value = """
                SELECT
                    e.id AS eventId,
                    e.name AS eventName,
                    s.id AS sessionId,
                    s.session_name AS sessionName,
                    s.start_date_time AS startDateTime,
                    s.end_date_time AS endDateTime,
                    COALESCE(GROUP_CONCAT(v.name SEPARATOR ', '), '') AS venueNames
                FROM event e
                JOIN session s ON s.event_id = e.id
                LEFT JOIN session_venue sv ON sv.session_id = s.id
                LEFT JOIN venue v ON v.id = sv.venue_id
                WHERE e.user_id = :organizerId AND e.status = 'ACTIVE'
                GROUP BY s.id
            """, nativeQuery = true)
    List<CalendarEventDTO> findCalendarEntriesByOrganizerId(@Param("organizerId") Integer organizerId);

    List<Event> findByEndDateTimeBeforeAndStatus(LocalDateTime nowDateTime, EventStatus eventStatus);

}
