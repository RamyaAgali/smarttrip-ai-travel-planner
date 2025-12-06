package com.smarttrip.authservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("smarttripteam@gmail.com");
        message.setTo(to);
        message.setSubject("SmartTrip - Password Reset");
        message.setText("Hello,\n\nClick the link below to reset your password:\n\n"
                + resetLink + "\n\nIf you didn’t request this, ignore this email.");

        mailSender.send(message);
    }
}
