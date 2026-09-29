package org.example.backend.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class AsyncMailSender {

    private static final Logger log = LoggerFactory.getLogger(AsyncMailSender.class);

    private final JavaMailSender javaMailSender;

    public AsyncMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void posaljiAsinhrono(String posiljalac, String prima, String naslov, String telo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(posiljalac);
        message.setTo(prima);
        message.setSubject(naslov);
        message.setText(telo);
        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            log.warn("Slanje mejla ka {} ('{}') nije uspelo: {}", prima, naslov, exception.getMessage());
        }
    }
}
