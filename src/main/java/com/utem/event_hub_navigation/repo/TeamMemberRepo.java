package com.utem.event_hub_navigation.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utem.event_hub_navigation.dto.TeamMemberDTO;
import com.utem.event_hub_navigation.model.TeamMember;
import com.utem.event_hub_navigation.model.TeamMemberKey;

public interface TeamMemberRepo extends JpaRepository<TeamMember, TeamMemberKey> {
    // Custom query methods can be added here if needed
    // For example, to find team members by event ID or user ID
    // List<TeamMember> findByEventId(Integer eventId);
    // List<TeamMember> findByUserId(Integer userId);

    // @Query(value = "SELECT new com.utem.event_hub_navigation.dto.TeamMemberDTO(u.id, u.name, u.email, r.name) " + 
    //         "FROM TeamMember tm " +
    //         "JOIN tm.user u " +
    //         "JOIN tm.role r " + // Join Role to get the name
    //         "WHERE tm.event.id = :eventId")
    // Page<TeamMemberDTO> findUserDetailsAndRoleByEventIdJPQL(@Param("eventId") Integer eventId, Pageable pageable);

    @Query(value = """
        SELECT 
            u.id as userId, 
            u.name as name, 
            u.email as email, 
            GROUP_CONCAT(r.name) as roles
        FROM team_member tm
        JOIN users u ON tm.user_id = u.id
        JOIN role r ON tm.role_id = r.id
        WHERE tm.event_id = :eventId
        GROUP BY u.id, u.name, u.email
        """, nativeQuery = true)
    Page<TeamMemberDTO> findTeamMembersWithRolesByEventId(@Param("eventId") Integer eventId,Pageable pageable);

}
