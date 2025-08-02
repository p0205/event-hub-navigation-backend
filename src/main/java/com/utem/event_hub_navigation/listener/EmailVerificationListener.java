package com.utem.event_hub_navigation.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.utem.event_hub_navigation.event.onVerifyEmailEvent;
import com.utem.event_hub_navigation.service.EmailService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationListener implements ApplicationListener<onVerifyEmailEvent> {

    private final EmailService emailService;


    @Override
    public void onApplicationEvent(onVerifyEmailEvent event) {
        this.sendVerificationCode(event);
    }

    private void sendVerificationCode(onVerifyEmailEvent event) {
        String email = event.getEmail();
        emailService.sendVerificationCode(email);
      

    }
}
