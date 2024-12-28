package com.example.food.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Healthy Table 인증 번호 입니다.");
        message.setText("인증 번호: " + code);
        message.setFrom("tjdduq410@naver.com"); // Optional: Define sender email
        mailSender.send(message);
    }
}