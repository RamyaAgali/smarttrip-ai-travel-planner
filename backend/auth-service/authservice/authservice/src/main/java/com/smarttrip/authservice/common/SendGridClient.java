package com.smarttrip.authservice.common;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SendGridClient {

    private final String apiKey = System.getenv("SENDGRID_API_KEY");
    private final String from = System.getenv("SENDGRID_FROM");

    public boolean sendEmail(String to, String subject, String html) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Mail mail = new Mail(fromEmail, subject, toEmail, new Content("text/html", html));

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("📨 SendGrid Status: " + response.getStatusCode());
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;

        } catch (IOException e) {
            System.err.println("❌ SendGrid Error: " + e.getMessage());
            return false;
        }
    }
}