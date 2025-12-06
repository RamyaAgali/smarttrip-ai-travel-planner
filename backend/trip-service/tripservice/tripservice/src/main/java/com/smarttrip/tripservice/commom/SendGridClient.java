package com.smarttrip.tripservice.commom;

import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

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
            System.out.println("📨 SendGrid Body: " + response.getBody());
            System.out.println("📨 SendGrid Headers: " + response.getHeaders());

            System.out.println("📨 SendGrid Status: " + response.getStatusCode());
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;

        } catch (IOException e) {
            System.err.println("❌ SendGrid Error: " + e.getMessage());
            return false;
        }
    }
    public void sendEmailWithAttachment(String to, String subject, String htmlContent, byte[] pdfBytes, String fileName) {
        try {
            Email fromEmail = new Email(System.getenv("SENDGRID_FROM_EMAIL"), "SmartTrip ✈");
            Email toEmail = new Email(to);

            Mail mail = new Mail();
            mail.setFrom(fromEmail);
            mail.setSubject(subject);

            Personalization personalization = new Personalization();
            personalization.addTo(toEmail);
            mail.addPersonalization(personalization);

            Content content = new Content("text/html", htmlContent);
            mail.addContent(content);

            // Attach PDF only if exists
            if (pdfBytes != null && pdfBytes.length > 0) {
                Attachments attachment = new Attachments();
                String base64File = Base64.getEncoder().encodeToString(pdfBytes);

                attachment.setFilename(fileName);
                attachment.setType("application/pdf");
                attachment.setDisposition("attachment");
                attachment.setContent(base64File);

                mail.addAttachments(attachment);
            }

            SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("📨 SendGrid Status: " + response.getStatusCode());
            System.out.println("📨 SendGrid Body: " + response.getBody());
            System.out.println("📨 SendGrid Headers: " + response.getHeaders());

            System.out.println("📨 SendGrid: Email sent with attachment to " + to);

        } catch (Exception e) {
            System.err.println("❌ SendGrid attachment email failed: " + e.getMessage());
        }
    }
}