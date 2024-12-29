package com.majdi.book_network.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String toWho,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {

        String templateName;

        if (emailTemplate == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplate.name();
        }

        //! creating MimeMessage (MIME is a protocol that allows email to include multiple types of content, such as plain text, HTML, attachments, images, and other multimedia elements, all within the same message.)
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED, //! Allows multiple parts (e.g., text and attachments) in the email.
                UTF_8.name() //! UTF-8 Encoding
        );

        //! HashMap to store Email properties that the email template needs
        Map<String, Object> properties = new HashMap<>();

        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activationCode", activationCode);

        //! A Context object (from Thymeleaf) that holds the properties, replacing placeholders with actual values
        Context context = new Context();
        context.setVariables(properties);

        //! setting MimeMessage Details
        helper.setFrom("majdiboubaker82@gmail.com");
        helper.setTo(toWho);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        //! sending email
        mailSender.send(mimeMessage);
    }

}
