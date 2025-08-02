// package com.utem.event_hub_navigation.service.impl;

// import java.time.LocalDateTime;
// import java.util.Random;
// import java.util.UUID;

// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import com.utem.event_hub_navigation.model.EmailVerificationCode;
// import com.utem.event_hub_navigation.model.PasswordResetCode;
// import com.utem.event_hub_navigation.model.User;
// import com.utem.event_hub_navigation.repo.PasswordResetCodeRepo;
// import com.utem.event_hub_navigation.repo.UserRepo;
// import com.utem.event_hub_navigation.service.EmailService;
// import com.utem.event_hub_navigation.service.PasswordResetService;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class PasswordResetServiceImpl implements PasswordResetService {

//     private final PasswordResetCodeRepo passwordResetCodeRepo;
//     private final UserRepo userRepo;
//     private final EmailService emailService;
//     private final PasswordEncoder passwordEncoder;

//     @Override
//     public PasswordResetCode createPasswordResetToken(String email) {
//         String code = String.format("%06d", new Random().nextInt(999999)); // 6-digit OTP

//         // delete existing tokens for same email
//         PasswordResetCode existingCode = passwordResetCodeRepo.findByEmail(email);
//         if (existingCode != null) {
//             emailVerificationCodeRepo.delete(existingCode);
//         }
//         EmailVerificationCode entity = EmailVerificationCode.builder()
//                 .email(email)
//                 .code(code)
//                 .verified(false)
//                 .expiryDate(LocalDateTime.now().plusMinutes(10))
//                 .build();

//         return emailVerificationCodeRepo.save(entity);
//     }


//     @Override
//     public boolean verifyCode(String email, String code) {
//         System.out.println("Verifying code for email: " + email + " with code: " + code);
//         EmailVerificationCode record = emailVerificationCodeRepo.findByEmailAndCode(email, code);
//         System.out.println("Found record: " + record);
//         if (record == null) return false;


//         if (record.getExpiryDate().isBefore(LocalDateTime.now())) {
//             emailVerificationCodeRepo.delete(record);
//             return false;
//         }

//         record.setVerified(true);
//         System.out.println("Setting record as verified: " + record.isVerified());
//         emailVerificationCodeRepo.save(record);
//         return true;
//     }



//     @Override
//     public void createPasswordResetToken(String email) {
//         User user = userRepo.findByEmail(email)
//             .orElseThrow(() -> new IllegalArgumentException("Email not found"));

//         String token = UUID.randomUUID().toString();

//         PasswordResetCode resetToken = PasswordResetCode.builder()
//             .token(token)
//             .user(user)
//             .expiryDate(LocalDateTime.now().plusMinutes(15))
//             .build();

//         tokenRepo.save(resetToken);

//         emailService.sendResetPasswordEmail(email, token);
//     }

//     @Override
//     public boolean validateToken(String token) {
//         return tokenRepo.findByToken(token)
//                 .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
//                 .isPresent();
//     }

//     @Override
//     public void resetPassword(String email, String newPassword) {
//         User user = userRepo.findByEmail(email)
//             .orElseThrow(() -> new IllegalArgumentException("Email not found"));

//         user.setPasswordHash(passwordEncoder.encode(newPassword)); // passwordEncoder = BCrypt
//         userRepo.save(user);

//     }
// }
