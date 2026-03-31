package com.stego.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailOtpProvider implements OtpProvider {

    @Autowired
    private EmailService emailService;

    @Override
    public void sendOtp(String email, String otp) {
        emailService.sendOtpEmail(email, otp);
    }
}
