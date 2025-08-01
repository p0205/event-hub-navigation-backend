package com.utem.event_hub_navigation.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
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
     * Send a plain text email
     *
     * @param to      Recipient email
     * @param subject Subject line
     * @param body    Email message content
     */

    private void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
            // You can also log this or throw a custom exception
        }
    }

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
}
