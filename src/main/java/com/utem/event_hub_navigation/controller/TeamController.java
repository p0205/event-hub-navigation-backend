package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.TeamMemberDTO;
import com.utem.event_hub_navigation.dto.UserDTOByTeamSearch;
import com.utem.event_hub_navigation.service.TeamService;

@RestController
@RequestMapping("/api/events/{eventId}/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // //Add team member
    // @PostMapping
    // public ResponseEntity<?> addTeamMember(@PathVariable("eventId") Integer
    // eventId, @RequestParam Integer userId, @RequestParam Integer roleId){
    // try {
    // teamService.addTeamMember(eventId, userId, roleId);

    // return ResponseEntity.status(HttpStatus.CREATED).build();
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // } catch (Exception e) {
    // return ResponseEntity.internalServerError().body("Failed to add member: " +
    // e.getMessage());
    // }
    // }

    // Add team member
    @PostMapping
    public ResponseEntity<?> addTeamMembers(@PathVariable("eventId") Integer eventId,
            @RequestParam List<Integer> userIds, @RequestParam Integer roleId) {
        try {
            teamService.addTeamMembers(eventId, userIds, roleId);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to add member: " + e.getMessage());
        }
    }

    // Get team members
    @GetMapping
    public ResponseEntity<?> getTeamMembers(
            @PathVariable("eventId") Integer eventId,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Pageable paging = PageRequest.of(pageNumber, pageSize);
            Page<TeamMemberDTO> members = teamService.getTeamMembers(eventId, paging);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get team members: " + e.getMessage());
        }
    }

    // search user to be assigned role
    @GetMapping("/search")
    public ResponseEntity<?> searchUserToAssignedRole(@PathVariable("eventId") Integer eventId,
            @RequestParam("query") String query, @RequestParam("roleId") Integer roleId) {

        // if (users == null || users.isEmpty()) {
        //     return ResponseEntity.notFound().build();
        // }

        // return ResponseEntity.ok(users);

        try {
            List<UserDTOByTeamSearch> users = teamService.searchUsers(eventId, query,roleId);
           
               
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get team members: " + e.getMessage());
        }

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeTeamMember(@PathVariable("eventId") Integer eventId,
            @PathVariable("userId") Integer userId) {
        try {
            System.out.println("Deleting team members...");
            teamService.removeTeamMember(eventId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            System.out.println(e.toString());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.toString());
            return ResponseEntity.internalServerError().body("Failed to remove member: " + e.getMessage());
        }
    }
}
