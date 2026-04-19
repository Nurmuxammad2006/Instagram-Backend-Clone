package com.example.instagrambackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendCodeToEmail(String toEmail, String code)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Your Verification Code - Chatting");
        helper.setText(buildEmailTemplate(code), true);

        mailSender.send(message);
    }

    private String buildEmailTemplate(String code) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                    <h2 style="color: #4F46E5;">Verify Your Email</h2>
                    <p>Enter this code to verify your email address:</p>
                    <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                color: #4F46E5; padding: 20px; background: #F3F4F6;
                                text-align: center; border-radius: 8px;">
                        %s
                    </div>
                    <p style="color: #6B7280;">This code expires in <b>5 minutes</b>.</p>
                    <p style="color: #6B7280;">If you didn't request this, ignore this email.</p>
                </div>
                """.formatted(code);
    }
}