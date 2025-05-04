package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.service.TeamService;

@RestController
@RequestMapping("/events/{eventId}/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // //Add team member 
    // @PostMapping
    // public ResponseEntity<?> addTeamMember(@PathVariable("eventId") Integer eventId, @RequestParam Integer userId, @RequestParam Integer roleId){
    //     try {
    //         teamService.addTeamMember(eventId, userId, roleId);

    //         return ResponseEntity.status(HttpStatus.CREATED).build();
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().body("Failed to add member: " + e.getMessage());
    //     }
    // }

    //Add team member 
    @PostMapping
    public ResponseEntity<?> addTeamMembers(@PathVariable("eventId") Integer eventId, @RequestParam List<Integer> userIds, @RequestParam Integer roleId){
        try {
            teamService.addTeamMembers(eventId, userIds, roleId);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add member: " + e.getMessage());
        }
    }


    //Remove team member
    //Get team members
    @GetMapping
    public ResponseEntity<?> getTeamMembers(@PathVariable("eventId") Integer eventId){
        try {
            return ResponseEntity.ok(teamService.getTeamMembers(eventId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get team members: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeTeamMember(@PathVariable("eventId") Integer eventId, @PathVariable("userId") Integer userId){
        try {
            teamService.removeTeamMember(eventId, userId);
            System.out.println("remove team member");
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to remove member: " + e.getMessage());
        }
    }
}
