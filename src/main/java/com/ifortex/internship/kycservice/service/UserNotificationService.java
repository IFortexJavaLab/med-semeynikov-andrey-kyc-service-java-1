package com.ifortex.internship.kycservice.service;

import com.ifortex.internship.medstarter.emailservice.EmailService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserNotificationService {

    static final String PARAMEDIC_APPROVAL_EMAIL_TEMPLATE = "paramedic-approval-email.html";
    static final String PARAMEDIC_REJECTION_EMAIL_TEMPLATE = "paramedic-rejection-email.html";

    EmailService emailService;

    public void sendApprovedApplicationEmail(String to, String subject, String loginLink) throws MessagingException {
        log.debug("Sending approved application email with subject '{}' to: {}", subject, to);

        String template = emailService.loadEmailTemplate(PARAMEDIC_APPROVAL_EMAIL_TEMPLATE);
        Map<String, String> replacements = new HashMap<>();
        replacements.put("user_email", to);
        replacements.put("login_url", loginLink);
        String content = emailService.populateTemplate(template, replacements);

        emailService.sendEmail(to, subject, content, true);
    }

    public void sendRejectedApplicationEmail(String to, String subject, String reason) throws MessagingException {
        log.debug("Sending approved application email with subject '{}' to: {}", subject, to);

        String template = emailService.loadEmailTemplate(PARAMEDIC_REJECTION_EMAIL_TEMPLATE);
        Map<String, String> replacements = new HashMap<>();
        replacements.put("user_email", to);
        replacements.put("reason", reason);//todo check what appears while reason is null
        String content = emailService.populateTemplate(template, replacements);

        emailService.sendEmail(to, subject, content, true);
    }
}
