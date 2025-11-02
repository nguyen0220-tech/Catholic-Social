package com.catholic.ac.kr.catholicsocial.security.systemservice;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendEmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("teemee202@gmail.com");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email", e);
        }

    }
}
