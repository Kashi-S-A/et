package com.et.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMailWithTemplate(String toEmail, String otp) throws Exception {

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("otp", otp);

        // Process Thymeleaf template
        String body = templateEngine.process("forgot-password-email", context);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Reset Password Request");
        helper.setText(body, true);  // true => HTML

        javaMailSender.send(message);

        System.out.println("Email sent to: " + toEmail);
    }
}
