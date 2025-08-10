package com.utem.event_hub_navigation.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.dto.EmailCheckResponse;
import com.utem.event_hub_navigation.dto.PasswordResetRequest;
import com.utem.event_hub_navigation.dto.SignInRequest;
import com.utem.event_hub_navigation.dto.SignUpRequest;
import com.utem.event_hub_navigation.dto.UserDTO;
import com.utem.event_hub_navigation.service.AuthService;
import com.utem.event_hub_navigation.service.EmailService;
import com.utem.event_hub_navigation.service.UserService;
import com.utem.event_hub_navigation.service.VerificationCodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final AuthService authService;

    private final VerificationCodeService emailVerificationCodeService;

    private final EmailService emailService;



    @GetMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean valid = emailVerificationCodeService.verifyCode(email, code);

        if (valid) {
            EmailCheckResponse response = userService.existInUTemDatabase(email);
            return ResponseEntity.ok(response.getUserDTO());
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification code.");
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> existInUTemDatabase(@RequestParam String email) {
        EmailCheckResponse response = userService.existInUTemDatabase(email);
        switch (response.getResult()) {
            case USER_ALREADY_REGISTERED:
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "User with this email is already registered."));
            case EMAIL_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Email not found in university database."));
            case VALID_EMAIL:
            emailService.sendVerificationCode(email);
                return ResponseEntity.ok(Map.of("message", "Email is valid and not registered."));
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Unknown error"));
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest req) {

        boolean success = userService.register(req.getEmail(), req.getPhoneNo(), req.getRawPassword());
        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful"));
        } else {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest req) {
        try {
            String token = authService.signIn(req);
            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.ofHours(1)) // Match your JWT expiry
                    .build();

            UserDTO authenticatUserDTO = userService.getUserByEmail(req.getEmail());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(authenticatUserDTO);
        } catch (AuthenticationException authException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.toString()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        UserDTO authenticatUserDTO = userService.getUserByEmail(email);
        return ResponseEntity.ok(authenticatUserDTO);
    }

     @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody PasswordResetRequest req) {
        try {
           
            userService.resetPassword(
                req.getEmail(), 
                req.getNewPassword()
            );

            return ResponseEntity
                .status(HttpStatus.OK)
                .body("Password reset successfully.");
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating password: " + e.getMessage());
        }
    }

    @GetMapping("/reset-password/send-code")
    public ResponseEntity<?> sendResetPasswordCode(
            @RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email format");
            }
            emailService.sendResetPasswordEmail(email);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body("Reset password code sent to " + email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid request parameters: " + e.getMessage()); 
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating password: " + e.getMessage());
        }
    }


    @GetMapping("/verify-password-reset-code")
    public ResponseEntity<?> verifyResetPasswordCode(@RequestParam String email, @RequestParam String code) {
        System.out.println("Verifying reset password code for email: " + email + " with code: " + code);
        boolean valid = emailVerificationCodeService.verifyCode(email, code);

        if (valid) {
            
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification code.");
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut() {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Strict")
                .maxAge(0) // Expire immediately
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful");
    }

}
