package com.utem.event_hub_navigation.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utem.event_hub_navigation.model.EmailVerificationCode;

public interface EmailVerificationCodeRepo extends JpaRepository<EmailVerificationCode, Long> {
    // Additional query methods can be defined here if needed
    // For example, find by token or email
    EmailVerificationCode findByCode(String code);
    EmailVerificationCode findByEmail(String email);
    EmailVerificationCode findByEmailAndCode(String email, String code);
    void deleteAllByExpiryDateBefore(LocalDateTime now);
    void deleteAllByEmail(String email);

}
