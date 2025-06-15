package com.utem.event_hub_navigation.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.Venue;
import com.utem.event_hub_navigation.model.VenueUtilizationData;

@Repository
public interface VenueRepo extends JpaRepository<Venue, Integer> {
    // Custom query methods can be defined here if needed
    // For example, find by name or location
    // List<Venue> findByName(String name);
    List<Venue> findByCapacityGreaterThanEqual(Integer capacity);

    @Query(value = """
        SELECT new com.utem.event_hub_navigation.model.VenueUtilizationData(
            v.id as venueId,
            v.name as venueName,
            COALESCE(v.capacity, 0) as venueCapacity,
            COALESCE(SUM(TIMESTAMPDIFF(HOUR, s.startDateTime, s.endDateTime)), 0) as totalHoursBooked,
            COALESCE(SUM(TIMESTAMPDIFF(HOUR, s.startDateTime, s.endDateTime)) * 100.0 / 
                NULLIF(TIMESTAMPDIFF(HOUR, :startDate, :endDate) * 24, 0), 0) as timeUtilizationRate,
            COUNT(DISTINCT s.id) as eventSessions,
            COALESCE(COUNT(DISTINCT r.id), 0) as totalRegisteredAttendance,
            COALESCE(COUNT(DISTINCT r.id) * 100.0 / NULLIF(COALESCE(v.capacity, 0), 0), 0) as averageRegisteredSeatOccupancy,
            COALESCE(COUNT(DISTINCT r.id) * 100.0 / NULLIF(COALESCE(v.capacity, 0) * COUNT(DISTINCT s.id), 0), 0) as overallSpaceUtilizationRate
        )
        FROM Venue v
        LEFT JOIN SessionVenue sv ON v.id = sv.venue.id
        LEFT JOIN Session s ON sv.session.id = s.id AND s.startDateTime BETWEEN :startDate AND :endDate
        LEFT JOIN Event e ON s.event.id = e.id
        LEFT JOIN Registration r ON e.id = r.event.id
        GROUP BY v.id, v.name, v.capacity
        ORDER BY v.name
    """)
    List<VenueUtilizationData> getVenueUtilizationData(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
