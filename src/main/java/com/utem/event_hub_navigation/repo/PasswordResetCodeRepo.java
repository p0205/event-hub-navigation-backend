// package com.utem.event_hub_navigation.repo;

// import java.time.LocalDateTime;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.utem.event_hub_navigation.model.PasswordResetCode;
// import com.utem.event_hub_navigation.model.User;

// public interface PasswordResetCodeRepo extends JpaRepository<PasswordResetCode, Long> {
//     Optional<PasswordResetCode> findByUserAndCode(User user, String Code);
//     void deleteAllByExpiryDateBefore(LocalDateTime now);
// }
