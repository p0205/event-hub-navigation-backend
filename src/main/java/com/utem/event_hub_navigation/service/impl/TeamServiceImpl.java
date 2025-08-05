package com.utem.event_hub_navigation.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utem.event_hub_navigation.dto.SimpleTeamEvent;
import com.utem.event_hub_navigation.dto.TeamMemberDTO;
import com.utem.event_hub_navigation.dto.UserDTOByTeamSearch;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.EventStatus;
import com.utem.event_hub_navigation.model.Role;
import com.utem.event_hub_navigation.model.TeamMember;
import com.utem.event_hub_navigation.model.TeamMemberKey;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.TeamMemberRepo;
import com.utem.event_hub_navigation.service.EmailService;
import com.utem.event_hub_navigation.service.EventService;
import com.utem.event_hub_navigation.service.RoleService;
import com.utem.event_hub_navigation.service.TeamService;
import com.utem.event_hub_navigation.service.UserService;

@Transactional
@Service
public class TeamServiceImpl implements TeamService {

    private final UserService userService;
    private final EventService eventService;
    private final RoleService roleService;
    private final TeamMemberRepo teamMemberRepo;
    private final EmailService emailService;

    @Autowired
    public TeamServiceImpl(UserService userService,
            EventService eventService,
            RoleService roleService,
            TeamMemberRepo teamMemberRepo,
            EmailService emailService) {
        this.emailService = emailService;
        this.userService = userService;
        this.eventService = eventService;
        this.roleService = roleService;
        this.teamMemberRepo = teamMemberRepo;
    }

    private void addTeamMemberRole(Event event, Role role, User user) throws Exception {

        TeamMemberKey teamMemberKey = new TeamMemberKey(event.getId(), user.getId(), role.getId());

        TeamMember teamMember = TeamMember.builder()
                .id(teamMemberKey)
                .user(user)
                .event(event)
                .role(role)
                .build();

        // Add team member to the event
        teamMemberRepo.save(teamMember);
    }

    // @Override
    public Page<TeamMemberDTO> getTeamMembers(Integer eventId, Pageable pageable) {
        // Check if the event exists
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        // Get team members for the event
        Page<TeamMemberDTO> teamMembers = teamMemberRepo.findTeamMembersWithRolesByEventId(eventId, pageable);

        return teamMembers;
    }

    @Override
    public void addTeamMembers(Integer eventId, List<Integer> userIds, Integer roleId) throws Exception {

        // Check if the event exists
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new Exception("Event not found");
        }

        // Check if the role exists
        Role role = roleService.getRoleById(roleId);
        if (role == null) {
            throw new Exception("Role not found");
        }

        for (Integer userId : userIds) {
                    // Check if the user exists
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new Exception("Users not found");
        }

            addTeamMemberRole(event, role, user);
            System.out.println(user.getEmail());
            // emailService.sendTeamAssignmentNotification(user.getEmail(), event.getName(), role.getName());
        }
    }

    @Override
    public void removeTeamMember(Integer eventId, Integer userId) {
        // Check if the event exists
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        // Check if the user exists
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("Users not found");
        }

        teamMemberRepo.deleteAllByEventAndUser(eventId, userId);
        System.out.println("Deleted successfully.");
    }

    @Override
    public List<UserDTOByTeamSearch> searchUsers(Integer eventId, String query, Integer roleId) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        return teamMemberRepo.findSearchedUsersByTeamAndRole(eventId, searchQuery, roleId);
    }

    @Override
    public Map<EventStatus, List<SimpleTeamEvent>> getTeamEvents(Integer userId) {
        // Check if the user exists
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Get team events for the user
        List<SimpleTeamEvent> teamEvents = teamMemberRepo.findTeamEventsByUserId(userId);

        return teamEvents.stream()
                .collect(Collectors.groupingBy(SimpleTeamEvent::getStatus));
    }

}
