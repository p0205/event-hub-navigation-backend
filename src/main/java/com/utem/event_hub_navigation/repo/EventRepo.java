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
import com.utem.event_hub_navigation.model.User;

@Repository
public interface EventRepo extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizerAndStatusOrderByStartDateTimeAsc(User organizer, EventStatus status);

    // List<Event> findByOrganizerAndStatusOrderByStartDateTimeDesc(Users organizer,
    // EventStatus status);

    // @Query("SELECT e FROM Event e WHERE e.startDateTime >= :startOfDay AND
    // e.startDateTime < :endOfDay")
    // List<Event> findByEventDate(@Param("startOfDay") LocalDateTime startOfDay,
    // @Param("endOfDay") LocalDateTime endOfDay);

    // You might also want to find events by just the organizer user

    List<Event> findByOrganizerOrderByStartDateTimeDesc(User organizer);

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
                WHERE e.user_id = :organizerId AND e.status = 'ACTIVE' AND s.start_date_time >= NOW()
                GROUP BY s.id
            """, nativeQuery = true)
    List<CalendarEventDTO> findCalendarEntriesByOrganizerId(@Param("organizerId") Integer organizerId);

    List<Event> findByEndDateTimeBeforeAndStatus(LocalDateTime nowDateTime, EventStatus eventStatus);

    /**
     * Fetches a single Event by its ID, eagerly loading its organizer,
     * team members (and their associated users/roles),
     * sessions, and the venues for each session.
     *
     * This uses a single SQL query (typically with LEFT JOINs) to avoid the N+1
     * problem.
     */
    @Query(value = """
                        SELECT
                          e.id as event_id, e.name, e.description, r.register_date,
                          e.start_date_time, e.end_date_time,
                          organizer.name as organizer_name,
                          pic.name as pic_name, pic.phone_no as pic_contact,
                          s.id as session_id, s.session_name,
                          s.start_date_time as session_start, s.end_date_time as session_end,
                          v.id as venue_id, v.name as venue_name
                        FROM event e

                        LEFT JOIN users organizer ON organizer.id = e.user_id
                        LEFT JOIN registration r ON r.event_id = e.id AND r.user_id = organizer.id

                        LEFT JOIN team_member t ON t.event_id = e.id
            LEFT JOIN role r2 ON r2.id = t.role_id AND r2.name = 'PIC'

                        LEFT JOIN users pic ON pic.id = t.user_id
                        LEFT JOIN session s ON s.event_id = e.id
                        LEFT JOIN session_venue vs ON vs.session_id = s.id
                        LEFT JOIN venue v ON v.id = vs.venue_id
                        WHERE e.id = :eventId

                                    """, nativeQuery = true)
    List<Object[]> findEventDetailsById(@Param("eventId") int eventId);
}