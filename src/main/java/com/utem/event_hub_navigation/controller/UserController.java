package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.PasswordUpdateDTO;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.dto.UserUpdateDTO;
import com.utem.event_hub_navigation.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/emails")
    public ResponseEntity<List<UserDTO>> getUsersByEmail(@RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmail(emails));
    }

    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email ) {
        UserDTO user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/name")
    public ResponseEntity<List<UserDTO>> getUserByName(@RequestParam String name ) {
        List<UserDTO> user = userService.getUserByNameLike(name);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/name_or_email")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String query) {

        List<UserDTO> users = userService.findByEmailOrName(query);

        if (users == null || users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(users);
    }

     @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer userId,
            @RequestBody PasswordUpdateDTO updateDTO) {
        
        boolean success = userService.updatePassword(
            userId, 
            updateDTO.getCurrentPassword(), 
            updateDTO.getNewPassword()
        );
        
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{userId}/phone")
    public ResponseEntity<?> updatePhoneNumber(
            @PathVariable Integer userId,
            @RequestBody UserUpdateDTO updateDTO) {
        
        if (updateDTO.getPhoneNo() == null || updateDTO.getPhoneNo().length() > 15) {
            return ResponseEntity.badRequest().build();
        }
        
        
        try {
            return ResponseEntity.ok( userService.updatePhoneNumber(userId, updateDTO.getPhoneNo()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    
        
    }
}
