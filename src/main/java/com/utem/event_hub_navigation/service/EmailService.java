package com.utem.event_hub_navigation.service;

public interface EmailService {

    void sendVerificationCode(String email);

    void sendResetPasswordEmail(String email);

}