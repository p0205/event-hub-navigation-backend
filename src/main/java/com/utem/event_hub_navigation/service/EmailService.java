package com.utem.event_hub_navigation.service;


public interface EmailService {

    void sendVerificationCode(String email);

    void sendResetPasswordEmail(String email);

    void sendTeamAssignmentNotification(String email, String eventName, String role);

}