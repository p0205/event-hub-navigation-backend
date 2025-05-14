package com.utem.event_hub_navigation.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.dto.TeamMemberDTO;
import com.utem.event_hub_navigation.model.Event;
import com.utem.event_hub_navigation.model.Role;
import com.utem.event_hub_navigation.model.TeamMember;
import com.utem.event_hub_navigation.model.TeamMemberKey;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.repo.TeamMemberRepo;
import com.utem.event_hub_navigation.service.EventService;
import com.utem.event_hub_navigation.service.RoleService;
import com.utem.event_hub_navigation.service.TeamService;
import com.utem.event_hub_navigation.service.UserService;

@Service
public class TeamServiceImpl implements TeamService {

    private final UserService userService;
    private final EventService eventService;
    private final RoleService roleService;
    private final TeamMemberRepo teamMemberRepo;

    @Autowired
    public TeamServiceImpl(UserService userService,
            EventService eventService,
            RoleService roleService,
            TeamMemberRepo teamMemberRepo) {
        this.userService = userService;
        this.eventService = eventService;
        this.roleService = roleService;
        this.teamMemberRepo = teamMemberRepo;
    }

    // Add team member
    @Override
    public void addTeamMemberRole(Integer eventId, Integer userId, Integer roleId) throws Exception {

        // Check if the user exists
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }

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


        TeamMemberKey teamMemberKey = new TeamMemberKey(userId, eventId,roleId);
        TeamMember teamMember = TeamMember.builder()
                .id(teamMemberKey)
                .user(user)
                .event(event)
                .build();

        // Add team member to the event
        teamMemberRepo.save(teamMember);
    }
    // Remove team member
    // Get team members

    // @Override
    public Page<TeamMemberDTO> getTeamMembers(Integer eventId, Pageable pageable) {
        // Check if the event exists
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        // Get team members for the event
        Page<TeamMemberDTO> teamMembers = teamMemberRepo.findTeamMembersWithRolesByEventId(eventId, pageable);

        System.out.println(teamMembers.toString());
        return teamMembers;
    }

    @Override
    public void addTeamMembers(Integer eventId, List<Integer> userIds, Integer roleId) throws Exception {
        for(Integer userId : userIds) {
           
                addTeamMemberRole(eventId, userId, roleId);
            
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
            throw new RuntimeException("User not found");
        }

        // Remove team member from the event
        TeamMemberKey teamMemberKey = TeamMemberKey.builder()
                                                    .eventId(eventId)
                                                    .userId(userId)
                                                    .build();
       
        teamMemberRepo.deleteById(teamMemberKey);
    }

    // Implement the methods here

}
