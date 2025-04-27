package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.service.UserService;

@RestController
@RequestMapping("/users")
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
}
