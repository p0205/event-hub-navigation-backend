package com.utem.event_hub_navigation.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.EmailVerificationCode;
import com.utem.event_hub_navigation.repo.EmailVerificationCodeRepo;
import com.utem.event_hub_navigation.service.VerificationCodeService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final EmailVerificationCodeRepo emailVerificationCodeRepo;

    @Override
    public EmailVerificationCode  generateAndSaveVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999)); // 6-digit OTP

        // delete existing tokens for same email
        EmailVerificationCode existingCode = emailVerificationCodeRepo.findByEmail(email);
        if (existingCode != null) {
            emailVerificationCodeRepo.delete(existingCode);
        }
        EmailVerificationCode entity = EmailVerificationCode.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        return emailVerificationCodeRepo.save(entity);
    }


    @Override
    public boolean verifyCode(String email, String code) {
        System.out.println("Verifying code for email: " + email + " with code: " + code);
        EmailVerificationCode record = emailVerificationCodeRepo.findByEmailAndCode(email, code);
        System.out.println("Found record: " + record);
        if (record == null) return false;


        if (record.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailVerificationCodeRepo.delete(record);
            return false;
        }

        record.setVerified(true);
        System.out.println("Setting record as verified: " + record.isVerified());
        emailVerificationCodeRepo.save(record);
        return true;
    }

    @Override
    public boolean isVerified(String email) {
        return emailVerificationCodeRepo.findByEmail(email).isVerified();
    }

}
