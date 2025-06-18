package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.PasswordUpdateDTO;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.model.User;
import com.utem.event_hub_navigation.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            // Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
            //     Sort.Direction.DESC : Sort.Direction.ASC;
            
            // PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            // Page<UserDTO> usersPage = userService.getAllUsers(pageRequest);
            
            // PaginatedResponse<UserDTO> response = new PaginatedResponse<>(
            //     usersPage.getContent(),
            //     usersPage.getNumber(),
            //     usersPage.getSize(),
            //     usersPage.getTotalElements(),
            //     usersPage.getTotalPages(),
            //     usersPage.isLast(),
            //     usersPage.isFirst()
            // );
            
              Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by("name"));
            return ResponseEntity.ok(userService.getAllUsers(paging));


        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid request parameters: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching users: " + e.getMessage());
        }
    }

    @GetMapping("/emails")
    public ResponseEntity<?> getUsersByEmail(@RequestBody List<String> emails) {
        try {
            List<UserDTO> users = userService.getUsersByEmail(emails);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching users by email: " + e.getMessage());
        }
    }

    @GetMapping("/email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            UserDTO user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + email);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user by email: " + e.getMessage());
        }
    }

    @GetMapping("/name")
    public ResponseEntity<?> getUserByName(@RequestParam String name) {
        try {
            List<UserDTO> users = userService.getUserByNameLike(name);
            if (users == null || users.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No users found with name containing: " + name);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching users by name: " + e.getMessage());
        }
    }

    @GetMapping("/name_or_email")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            List<UserDTO> users = userService.findByEmailOrName(query);
            if (users == null || users.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No users found matching query: " + query);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error searching users: " + e.getMessage());
        }
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Integer userId,
            @RequestBody PasswordUpdateDTO updateDTO) {
        try {
            boolean success = userService.updatePassword(
                userId, 
                updateDTO.getCurrentPassword(), 
                updateDTO.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Failed to update password. Please check your current password.");
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating password: " + e.getMessage());
        }
    }

    @PatchMapping("/{userId}/update")
    public ResponseEntity<?> updatePhoneNumber(
            @PathVariable Integer userId,
            @RequestBody UserDTO updateDTO) {
        try {
            if (updateDTO.getPhoneNo() == null || updateDTO.getPhoneNo().length() > 15) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid phone number format");
            }
            
            UserDTO updatedUser = userService.updateUserInfo(userId, updateDTO);
            if (updatedUser == null) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + userId);
            }
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating phone number: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Integer userId) {
        try {
            
            
           userService.deleteUser(userId);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating phone number: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOutsiderAccount(@RequestBody UserDTO accountDTO) {
        try {
            if (accountDTO.getName() == null || accountDTO.getEmail() == null || 
                accountDTO.getPhoneNo() == null || accountDTO.getGender() == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("All fields are required");
            }

            if (!accountDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email format");
            }

            if (!accountDTO.getPhoneNo().matches("^\\d{10,15}$")) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid phone number format");
            }

            if (accountDTO.getGender() != 'M' && accountDTO.getGender() != 'F') {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Gender must be 'M' or 'F'");
            }

            User user = userService.createOutsiderAccount(
                accountDTO.getName(),
                accountDTO.getEmail(),
                accountDTO.getPhoneNo(),
                accountDTO.getGender()
            );

            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while creating the account: " + e.getMessage());
        }
    }

  

    @PatchMapping("/{userId}/update_temp_password")
    public ResponseEntity<?> updateOutsiderPassword(@PathVariable Integer userId, @RequestBody PasswordUpdateDTO updateDTO) {
        try {
            if (updateDTO.getOutsiderNewPassword() == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("New password must be at least 6 characters long");
            }
            boolean success = userService.updateOutsiderPassword(userId, updateDTO.getOutsiderNewPassword());
            if (!success) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + userId);
            }   
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while updating the password: " + e.getMessage());
        }
    }

   
}
