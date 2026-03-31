package com.stego.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@stego.com}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            // In a production app, we might retry or store in a 'failed_emails' table
        }
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "Stego — Your Verification Code";
        String body = "<h3>Welcome to Stego!</h3>" +
                "<p>Your one-time password (OTP) for login/registration is:</p>" +
                "<h2 style='color: #ff5a1f;'>" + otp + "</h2>" +
                "<p>This code will expire in 5 minutes.</p>" +
                "<br><p>Save Time, Eat & Go.</p>";
        
        sendEmail(email, subject, body);
    }
}
