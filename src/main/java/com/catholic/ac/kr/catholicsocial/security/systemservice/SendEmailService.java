package com.catholic.ac.kr.catholicsocial.security.systemservice;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendEmailService {
    private final Resend resend;

    public SendEmailService(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    /*
        No verify domain
        test
        => from: onboarding@resend.dev,  to:teemee202@gmail.com
     */
    public void sendEmail(String to, String subject, String body) {
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to("teemee202@gmail.com")
                .subject(subject)
                .html(body)
                .build();

        try {
            resend.emails().send(request);
            log.info("Send email successful to: {} ", to);
        } catch (Exception e) {
            log.error("Send email failed: {}", e.getMessage());
        }
    }
}
