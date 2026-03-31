package com.stego.backend.service;

public interface OtpProvider {
    /**
     * Sends an OTP (One-Time Password) to the specified destination (Email/Phone).
     * @param destination The target address (email or phone number)
     * @param otp The 6-digit code to send
     */
    void sendOtp(String destination, String otp);
}
