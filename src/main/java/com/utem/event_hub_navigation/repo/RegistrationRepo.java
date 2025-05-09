package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.dto.DemographicDataDTO;
import com.utem.event_hub_navigation.dto.DemographicDataRow;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Registration;
import com.utem.event_hub_navigation.model.User;

@Repository
public interface RegistrationRepo extends JpaRepository<Registration, Integer> {

    Registration findByEventAndParticipant(Event event, User user);

    List<Registration> findByEvent(Event event);

    Boolean existsByEventAndParticipant(Event event, User user);

    @Query("SELECT count(r.id) FROM Registration r WHERE r.event.id = :eventId")
    int countByEventId(Integer eventId);


    @Query("""
        SELECT new com.utem.event_hub_navigation.dto.DemographicDataRow(
            u.faculty, 
            COUNT(u.id), 
            (COUNT(u.id) * 100.0 / (SELECT COUNT(r.id) FROM Registration r WHERE r.event.id = :eventId))
        )
        FROM Registration r
        JOIN r.participant u
        WHERE r.event.id = :eventId
        GROUP BY u.faculty
        """)
List<DemographicDataRow> getDemographicDataGroupByFaculty(@Param("eventId") Integer eventId);

@Query("""
        SELECT new com.utem.event_hub_navigation.dto.DemographicDataRow(
            u.course, 
            COUNT(u.id), 
            (COUNT(u.id) * 100.0 / (SELECT COUNT(r.id) FROM Registration r WHERE r.event.id = :eventId))
        )
        FROM Registration r
        JOIN r.participant u
        WHERE r.event.id = :eventId
        GROUP BY u.course
        """)
List<DemographicDataRow> getDemographicDataGroupByCourse(@Param("eventId") Integer eventId);

@Query("""
        SELECT new com.utem.event_hub_navigation.dto.DemographicDataRow(
            u.year, 
            COUNT(u.id), 
            (COUNT(u.id) * 100.0 / (SELECT COUNT(r.id) FROM Registration r WHERE r.event.id = :eventId))
        )
        FROM Registration r
        JOIN r.participant u
        WHERE r.event.id = :eventId
        GROUP BY u.year
        """)
List<DemographicDataRow> getDemographicDataGroupByYear(@Param("eventId") Integer eventId);

@Query("""
        SELECT new com.utem.event_hub_navigation.dto.DemographicDataRow(
            CASE 
                WHEN u.gender = 'M' THEN 'Male' 
                WHEN u.gender = 'F' THEN 'Female' 
                ELSE 'Other'
            END, 
            COUNT(u.id), 
            (COUNT(u.id) * 100.0 / (SELECT COUNT(r.id) FROM Registration r WHERE r.event.id = :eventId))
        )
        FROM Registration r
        JOIN r.participant u
        WHERE r.event.id = :eventId
        GROUP BY u.gender
        """)
List<DemographicDataRow> getDemographicDataGroupByGender(@Param("eventId") Integer eventId);

}
