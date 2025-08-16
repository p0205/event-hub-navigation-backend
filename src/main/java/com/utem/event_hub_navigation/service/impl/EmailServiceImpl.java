package com.utem.event_hub_navigation.service.impl;

import lombok.RequiredArgsConstructor;


import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.EmailVerificationCode;
import com.utem.event_hub_navigation.service.EmailService;
import com.utem.event_hub_navigation.service.VerificationCodeService;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final String FROM_EMAIL = "eventhub@utem.edu.my"; // Change this

    private final VerificationCodeService verificationCodeService;

    /**
     * Send a html text email
     *
     * @param to      Recipient email
     * @param subject Subject line
     * @param body    Email message content in HTML format
     */

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            helper.setFrom(new InternetAddress(FROM_EMAIL));

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Failed to send HTML email: " + e.getMessage());
        }
    }

    @Override
    public void sendVerificationCode(String email) {
        EmailVerificationCode verificationCode = verificationCodeService.generateAndSaveVerificationCode(email);

        String code = verificationCode.getCode();
        String subject = "FTMK Event Hub Verification Code";
        String html = "<p>Your verification code is:</p>" +
                "<h2>" + code + "</h2>" +
                "<p>This code expires in 10 minutes.</p>";
        sendHtmlEmail(email, subject, html);
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        EmailVerificationCode verificationCode = verificationCodeService.generateAndSaveVerificationCode(email);

        String code = verificationCode.getCode();
        String subject = "FTMK Event Hub: Use OTP to Reset Your Password";
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Password Reset OTP</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>"
                +
                "    <h2 style='color: #003366;'>FTMK Event Hub</h2>" +
                "    <p>Hello,</p>" +
                "    <p>You have requested to reset your password. Please use the following One-Time Password (OTP) to proceed:</p>"
                +
                "    <p style='font-size: 24px; font-weight: bold; color: #d9534f; text-align: center;'>" + code
                + "</p>" +
                "    <p>This OTP is valid for 10 minutes. Do not share it with anyone.</p>" +
                "    <p>If you did not request this reset, please ignore this email.</p>" +
                "    <br>" +
                "    <p>Best regards,<br>FTMK Event Hub Team</p>" +
                "    <hr style='margin-top: 40px;'>" +
                "    <p style='font-size: 12px; color: #888888;'>This is an automated email, please do not reply to it.</p>"
                +
                "  </div>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(email, subject, html);
    }

    @Override
    public void sendTeamAssignmentNotification(String email, String eventName, String role) {

        String subject = "FTMK Event Hub – Team Assignment for \"" + eventName + "\"";

        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Team Assignment Notification</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "  <div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>"
                +
                "    <h2 style='color: #003366;'>FTMK Event Hub</h2>" +
                "    <p>Hello,</p>" +
                "    <p>You have been successfully added to the event team for:</p>" +
                "    <h3 style='color: #007bff;'>" + eventName + "</h3>" +
                "    <p>Your assigned role: <strong>" + role + "</strong></p>" +
                "    <p>We look forward to your contribution and teamwork in making this event a success.</p>" +
                "    <br>" +
                "    <p>Best regards,<br>FTMK Event Hub Team</p>" +
                "    <hr style='margin-top: 40px;'>" +
                "    <p style='font-size: 12px; color: #888888;'>This is an automated email, please do not reply to it.</p>"
                +
                "  </div>" +
                "</body>" +
                "</html>";
        sendHtmlEmail(email, subject, html);

    }
}
