//package com.smarttrip.tripservice.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import jakarta.mail.util.ByteArrayDataSource;
//
//import java.io.ByteArrayInputStream;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.core.io.InputStreamSource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import com.smarttrip.tripservice.commom.SendGridClient;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//    
//    @Autowired
//    private SendGridClient sendGridClient;
//
//    // ✅ Common method for successful payment
//    public void sendPaymentConfirmation(
//            String email,
//            String destination,
//            double amount,
//            String currency,
//            String startDate,
//            String endDate,
//            String travelMode,
//            byte[] pdfBytes
//    ) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip ✈ Payment Successful – Your Trip to " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background-color:#f4f8fb; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1)">
//                    <div style="background-color:#2563eb; color:white; padding:15px; text-align:center;">
//                      <h2>SmartTrip 🌍</h2>
//                      <p style="margin:0;">Your Travel Companion for Smart Planning</p>
//                    </div>
//                    <div style="padding:20px;">
//                      <h3 style="color:#2563eb;">Hi Traveler,</h3>
//                      <p>Your payment for <strong>%s</strong> has been successfully processed! 🎉</p>
//                      <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
//                        <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
//                        <tr><td><strong>Travel Dates:</strong></td><td>%s → %s</td></tr>
//                        <tr><td><strong>Travel Mode:</strong></td><td>%s</td></tr>
//                        <tr><td><strong>Payment Amount:</strong></td><td>%s %.2f</td></tr>
//                        <tr><td><strong>Status:</strong></td><td style="color:green;">Confirmed ✅</td></tr>
//                      </table>
//                      <p>We've attached your <strong>Trip Invoice PDF</strong> below. You can also view your full itinerary anytime from your SmartTrip dashboard.</p>
//                      <div style="text-align:center; margin-top:25px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#2563eb; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
//                      </div>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    </div>
//                    <div style="background:#f1f5f9; padding:15px; text-align:center; font-size:13px; color:#666;">
//                      <p>Thank you for trusting <strong>SmartTrip</strong> with your journey! ✈</p>
//                      <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
//                    </div>
//                  </div>
//                </div>
//                """.formatted(destination, destination, startDate, endDate, travelMode,currency, amount);
//
//            helper.setText(htmlContent, true);
//
////            if (pdfBytes != null && pdfBytes.length > 0) {
////                helper.addAttachment("SmartTrip-Invoice.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));
////            }
//         // ✅ Attach PDF invoice
//          if (pdfBytes != null && pdfBytes.length > 0) {
//              InputStreamSource attachment = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
//              helper.addAttachment("SmartTrip-Invoice.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));
//          }
////            mailSender.send(message);
//          	String subject = "SmartTrip ✈ Payment Successful – Your Trip to " + destination;
//          	sendGridClient.sendEmail(email, subject,htmlContent);
//            System.out.println("✅ Payment confirmation email sent to " + email);
//
//        } catch (MessagingException e) {
//            System.err.println("⚠ Failed to send email: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("⚠ Unexpected email error: " + e.getMessage());
//        }
//    }
//
//    // ❌ New: for failed or cancelled payments
//    public void sendPaymentFailure(String email, String destination, double amount, String currency, String reason) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip ⚠ Payment Failed – Trip to " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background-color:#fff5f5; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1)">
//                    <div style="background-color:#dc2626; color:white; padding:15px; text-align:center;">
//                      <h2>Payment Failed ❌</h2>
//                    </div>
//                    <div style="padding:20px;">
//                      <h3 style="color:#dc2626;">Hello Traveler,</h3>
//                      <p>We’re sorry, but your payment for the trip to <strong>%s</strong> could not be processed.</p>
//                      <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
//                        <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
//                        <tr><td><strong>Travel Mode:</strong></td><td>%s</td></tr>
//                        <tr><td><strong>Amount Attempted:</strong></td><td>%s %.2f</td></tr>
//                        <tr><td><strong>Reason:</strong></td><td style="color:#dc2626;">%s</td></tr>
//                      </table>
//                      <p>Please try again by logging into your SmartTrip dashboard and re-attempting the payment.</p>
//                      <div style="text-align:center; margin-top:25px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#dc2626; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">Retry Payment</a>
//                      </div>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    </div>
//                    <div style="background:#fef2f2; padding:15px; text-align:center; font-size:13px; color:#666;">
//                      <p>Need help? Contact support@smarttrip.com ✉</p>
//                    </div>
//                  </div>
//                </div>
//                """.formatted(destination, destination, currency,amount, reason);
//
//            helper.setText(htmlContent, true);
////            mailSender.send(message);
//            String subject = "SmartTrip ⚠ Payment Failed – Trip to " + destination;
//            sendGridClient.sendEmail(email, subject, htmlContent);
//            
//            System.out.println("⚠ Payment failure email sent to " + email);
//
//        } catch (Exception e) {
//            System.err.println("💥 Failed to send payment failure email: " + e.getMessage());
//        }
//    }
//    public void sendRefundInitiated(String email, String destination, double amount, String currency, String orderId) {
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
//
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip 💸 Refund Initiated – " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background:#f0fdf4; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
//                    <div style="background:#16a34a; color:white; padding:15px; text-align:center;">
//                      <h2>Refund Initiated 💰</h2>
//                    </div>
//                    <div style="padding:20px;">
//                      <p>Your payment for the trip to <strong>%s</strong> (Order ID: %s) has been successfully cancelled.</p>
//                      <p>A refund of <strong>%s %.2f</strong> will be credited to your original payment method within 3–5 business days.</p>
//                      <div style="text-align:center; margin-top:20px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#16a34a; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
//                      </div>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    </div>
//                  </div>
//                </div>
//            """.formatted(destination, orderId, currency, amount);
//
//            helper.setText(htmlContent, true);
////            mailSender.send(msg);
//            String subject = "SmartTrip 💸 Refund Initiated – " + destination;
//            sendGridClient.sendEmail(email, subject, htmlContent);
//            System.out.println("✅ Refund initiated email sent to " + email);
//        } catch (Exception e) {
//            System.err.println("⚠ Failed to send refund email: " + e.getMessage());
//        }
//    }
//    public void sendTripCancellation(String email, String destination, String startDate, String endDate) {
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
//
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip ❌ Trip Cancelled – " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background:#fff7ed; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
//                    <div style="background:#f97316; color:white; padding:15px; text-align:center;">
//                      <h2>Trip Cancelled ❌</h2>
//                    </div>
//                    <div style="padding:20px;">
//                      <p>Your trip to <strong>%s</strong> scheduled from <strong>%s → %s</strong> has been successfully cancelled.</p>
//                      <p>If any payment was made, the refund process will be initiated and you’ll receive a separate confirmation email.</p>
//                      <div style="text-align:center; margin-top:20px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#f97316; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
//                      </div>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    </div>
//                  </div>
//                </div>
//            """.formatted(destination, startDate, endDate);
//
//            helper.setText(htmlContent, true);
////            mailSender.send(msg);
//            String subject = "SmartTrip ❌ Trip Cancelled – " + destination;
//            sendGridClient.sendEmail(email, subject, htmlContent);
//            System.out.println("📨 Cancellation email sent to " + email);
//        } catch (Exception e) {
//            System.err.println("⚠ Failed to send trip cancellation email: " + e.getMessage());
//        }
//    }
//    public void sendTripRebooked(String email, String destination, String startDate, String endDate) {
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
//            
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip 🔁 Trip Rebooked – " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background:#ecfdf5; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
//                    <div style="background:#059669; color:white; padding:15px; text-align:center;">
//                      <h2>Trip Rebooked Successfully 🔁</h2>
//                    </div>
//                    <div style="padding:20px;">
//                      <p>Hi Traveler,</p>
//                      <p>Your trip to <strong>%s</strong> has been successfully <strong>rebooked</strong> within 24 hours of cancellation! 🎉</p>
//                      <p>Your travel dates remain the same:</p>
//                      <p><strong>%s → %s</strong></p>
//                      <p>You can view your updated itinerary from your dashboard.</p>
//                      <div style="text-align:center; margin-top:20px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#059669; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    <div style="background:#f1f5f9; padding:15px; text-align:center; font-size:13px; color:#666;">
//                      <p>Thank you for choosing <strong>SmartTrip</strong>! ✈</p>
//                      <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
//                    </div>
//                  </div>
//                </div>
//            """.formatted(destination, startDate, endDate);
//
//            helper.setText(htmlContent, true);
////            mailSender.send(msg);
//            String subject = "SmartTrip 🔁 Trip Rebooked – " + destination;
//            sendGridClient.sendEmail(email, subject, htmlContent);
//            System.out.println("✅ Rebook confirmation email sent to " + email);
//        } catch (Exception e) {
//            System.err.println("⚠ Failed to send rebook email: " + e.getMessage());
//        }
//    }
//    public void sendTripBookedPendingPayment(String email, String destination, String startDate, String endDate, double amount, String currency) {
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
//
//            helper.setFrom("SmartTrip <9d7735001@smtp-brevo.com>","SmartTrip ✈");
//            helper.setTo(email);
//            helper.setSubject("SmartTrip 🧳 Trip Booked – Complete Payment to Confirm " + destination);
//
//            String htmlContent = """
//                <div style="font-family: Arial, sans-serif; background:#eef2ff; padding:20px;">
//                  <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
//                    <div style="background:#4f46e5; color:white; padding:15px; text-align:center;">
//                      <h2>Trip Booked Successfully ✈</h2>
//                    </div>
//                    <div style="padding:20px;">
//                      <p>Hi Traveler,</p>
//                      <p>Your trip to <strong>%s</strong> has been <strong>booked</strong> successfully.</p>
//                      <p>To confirm your travel, please complete the payment as soon as possible.</p>
//                      <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
//                        <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
//                        <tr><td><strong>Travel Dates:</strong></td><td>%s → %s</td></tr>
//                        <tr><td><strong>Amount Due:</strong></td><td>%s %.2f</td></tr>
//                        <tr><td><strong>Status:</strong></td><td style="color:#f59e0b;">Awaiting Payment ⏳</td></tr>
//                      </table>
//                      <div style="text-align:center; margin-top:25px;">
//                        <a href="http://localhost:5173/my-trips" style="background:#4f46e5; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">Complete Payment</a>
//                      </div>
//                      <p style="margin-top:20px; font-size:13px; color:#555;">
//                        ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
//                      </p>
//                    </div>
//                    <div style="background:#f9fafb; padding:15px; text-align:center; font-size:13px; color:#666;">
//                      <p>Thank you for planning your journey with <strong>SmartTrip</strong> 🌍</p>
//                      <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
//                    </div>
//                  </div>
//                </div>
//            """.formatted(destination, destination, startDate, endDate, currency, amount);
//
//            helper.setText(htmlContent, true);
////            mailSender.send(msg);
//            String subject = "SmartTrip 🧳 Trip Booked – Complete Payment to Confirm " + destination;
//            sendGridClient.sendEmail(email, subject, htmlContent);
//            System.out.println("📨 Trip booked (pending payment) email sent to " + email);
//        } catch (Exception e) {
//            System.err.println("⚠ Failed to send booked pending payment email: " + e.getMessage());
//        }
//    }
//}
package com.smarttrip.tripservice.service;

import com.smarttrip.tripservice.commom.SendGridClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private SendGridClient sendGridClient;

    // ========= PAYMENT SUCCESS ==========
    public void sendPaymentConfirmation(
            String email,
            String destination,
            double amount,
            String currency,
            String startDate,
            String endDate,
            String travelMode,
            byte[] pdfBytes
    ) {
        try {
            String subject = "SmartTrip ✈ Payment Successful – Your Trip to " + destination;

            String htmlContent = """
                  <div style="font-family: Arial, sans-serif; background-color:#f4f8fb; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1)">
                      <div style="background-color:#2563eb; color:white; padding:15px; text-align:center;">
                        <h2>SmartTrip 🌍</h2>
                        <p style="margin:0;">Your Travel Companion for Smart Planning</p>
                      </div>
                      <div style="padding:20px;">
                        <h3 style="color:#2563eb;">Hi Traveler,</h3>
                        <p>Your payment for <strong>%s</strong> has been successfully processed! 🎉</p>
                        <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
                          <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
                          <tr><td><strong>Travel Dates:</strong></td><td>%s → %s</td></tr>
                          <tr><td><strong>Travel Mode:</strong></td><td>%s</td></tr>
                          <tr><td><strong>Payment Amount:</strong></td><td>%s %.2f</td></tr>
                          <tr><td><strong>Status:</strong></td><td style="color:green;">Confirmed ✅</td></tr>
                        </table>
                        <p>We've attached your <strong>Trip Invoice PDF</strong> below. You can also view your full itinerary anytime from your SmartTrip dashboard.</p>
                        <div style="text-align:center; margin-top:25px;">
                          <a href="http://localhost:5173/my-trips" style="background:#2563eb; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
                        </div>
                        </div>
                        <p style="margin-top:20px; font-size:13px; color:#555;">
                          ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                        </p>
                      </div>
                      </div>
                      <div style="background:#f1f5f9; padding:15px; text-align:center; font-size:13px; color:#666;">
                        <p>Thank you for trusting <strong>SmartTrip</strong> with your journey! ✈</p>
                        <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
                      </div>
                    </div>
                  </div>
                  """.formatted(destination, destination, startDate, endDate, travelMode, currency, amount);

            sendGridClient.sendEmailWithAttachment(
                    email,
                    subject,
                    htmlContent,
                    pdfBytes
            );

            System.out.println("✅ Payment confirmation email sent (with invoice) to " + email);

        } catch (Exception e) {
            System.err.println("⚠ Email send failed: " + e.getMessage());
        }
    }

    // ========= PAYMENT FAILURE ==========
    public void sendPaymentFailure(String email, String destination, double amount, String currency, String reason) {
        try {
            String subject = "SmartTrip ⚠ Payment Failed – " + destination;

            String html = """
                    <div style="font-family: Arial, sans-serif; background-color:#fff5f5; padding:20px;">
                      <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1)">
                        <div style="background-color:#dc2626; color:white; padding:15px; text-align:center;">
                          <h2>Payment Failed ❌</h2>
                        </div>
                        <div style="padding:20px;">
                          <h3 style="color:#dc2626;">Hello Traveler,</h3>
                          <p>We’re sorry, but your payment for the trip to <strong>%s</strong> could not be processed.</p>
                          <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
                            <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
                            <tr><td><strong>Travel Mode:</strong></td><td>%s</td></tr>
                            <tr><td><strong>Amount Attempted:</strong></td><td>%s %.2f</td></tr>
                            <tr><td><strong>Reason:</strong></td><td style="color:#dc2626;">%s</td></tr>
                          </table>
                          <p>Please try again by logging into your SmartTrip dashboard and re-attempting the payment.</p>
                          <div style="text-align:center; margin-top:25px;">
                            <a href="http://localhost:5173/my-trips" style="background:#dc2626; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">Retry Payment</a>
                          </div>
                          </div>
                          <p style="margin-top:20px; font-size:13px; color:#555;">
                            ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                          </p>
                        </div>
                        </div>
                        <div style="background:#fef2f2; padding:15px; text-align:center; font-size:13px; color:#666;">
                          <p>Need help? Contact support@smarttrip.com ✉</p>
                        </div>
                      </div>
                    </div>
                    """ .formatted(destination, reason, currency, amount);

            sendGridClient.sendEmail(email, subject, html);
            System.out.println("⚠ Payment failure email sent to " + email);

        } catch (Exception e) {
            System.err.println("⚠ Failed to send payment failure email: " + e.getMessage());
        }
    }

    // ========= REFUND ==========
    public void sendRefundInitiated(String email, String destination, double amount, String currency, String orderId) {
        try {
            String subject = "SmartTrip 💸 Refund Initiated – " + destination;

            String html = """
                    <div style="font-family: Arial, sans-serif; background:#f0fdf4; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
                      <div style="background:#16a34a; color:white; padding:15px; text-align:center;">
                        <h2>Refund Initiated 💰</h2>
                      </div>
                      <div style="padding:20px;">
                        <p>Your payment for the trip to <strong>%s</strong> (Order ID: %s) has been successfully cancelled.</p>
                        <p>A refund of <strong>%s %.2f</strong> will be credited to your original payment method within 3–5 business days.</p>
                        <div style="text-align:center; margin-top:20px;">
                          <a href="http://localhost:5173/my-trips" style="background:#16a34a; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
                        </div>
                        </div>
                        <p style="margin-top:20px; font-size:13px; color:#555;">
                          ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                        </p>
                      </div>
                      </div>
                    </div>
                  </div>
              """.formatted(destination, orderId, currency, amount );

            sendGridClient.sendEmail(email, subject, html);
            System.out.println("💸 Refund email sent to " + email);

        } catch (Exception e) {
            System.err.println("⚠ Refund email failed: " + e.getMessage());
        }
    }

    // ========= CANCELLATION ==========
    public void sendTripCancellation(String email, String destination, String startDate, String endDate) {
        try {
            String subject = "SmartTrip ❌ Trip Cancelled – " + destination;

            String html = """
                    <div style="font-family: Arial, sans-serif; background:#fff7ed; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
                      <div style="background:#f97316; color:white; padding:15px; text-align:center;">
                        <h2>Trip Cancelled ❌</h2>
                      </div>
                      <div style="padding:20px;">
                        <p>Your trip to <strong>%s</strong> scheduled from <strong>%s → %s</strong> has been successfully cancelled.</p>
                        <p>If any payment was made, the refund process will be initiated and you’ll receive a separate confirmation email.</p>
                        <div style="text-align:center; margin-top:20px;">
                          <a href="http://localhost:5173/my-trips" style="background:#f97316; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
                        </div>
                        </div>
                        <p style="margin-top:20px; font-size:13px; color:#555;">
                          ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                        </p>
                      </div>
                      </div>
                    </div>
                  </div>
              """.formatted(destination, startDate, endDate);

            sendGridClient.sendEmail(email, subject, html);
            System.out.println("📨 Cancellation email sent");

        } catch (Exception e) {
            System.err.println("⚠ Cancellation email failed: " + e.getMessage());
        }
    }

    // ========= REBOOK ==========
    public void sendTripRebooked(String email, String destination, String startDate, String endDate) {
        try {
            String subject = "SmartTrip 🔁 Trip Rebooked – " + destination;

            String html = """
                    <div style="font-family: Arial, sans-serif; background:#ecfdf5; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
                      <div style="background:#059669; color:white; padding:15px; text-align:center;">
                        <h2>Trip Rebooked Successfully 🔁</h2>
                      </div>
                      <div style="padding:20px;">
                        <p>Hi Traveler,</p>
                        <p>Your trip to <strong>%s</strong> has been successfully <strong>rebooked</strong> within 24 hours of cancellation! 🎉</p>
                        <p>Your travel dates remain the same:</p>
                        <p><strong>%s → %s</strong></p>
                        <p>You can view your updated itinerary from your dashboard.</p>
                        <div style="text-align:center; margin-top:20px;">
                          <a href="http://localhost:5173/my-trips" style="background:#059669; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">View My Trips</a>
                        </div>
                        <p style="margin-top:20px; font-size:13px; color:#555;">
                          ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                        </p>
                      </div>
                      <div style="background:#f1f5f9; padding:15px; text-align:center; font-size:13px; color:#666;">
                        <p>Thank you for choosing <strong>SmartTrip</strong>! ✈</p>
                        <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
                      </div>
                    </div>
                  </div>
              """.formatted(destination, startDate, endDate);

            sendGridClient.sendEmail(email, subject, html);
            System.out.println("🔁 Rebook email sent");

        } catch (Exception e) {
            System.err.println("⚠ Rebook email failed: " + e.getMessage());
        }
    }

    // ========= BOOKED PENDING PAYMENT ==========
    public void sendTripBookedPendingPayment(String email, String destination, String startDate, String endDate, double amount, String currency) {
        try {
            String subject = "SmartTrip 🧳 Complete Payment – " + destination;

            String html = """
                    <div style="font-family: Arial, sans-serif; background:#eef2ff; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.08)">
                      <div style="background:#4f46e5; color:white; padding:15px; text-align:center;">
                        <h2>Trip Booked Successfully ✈</h2>
                      </div>
                      <div style="padding:20px;">
                        <p>Hi Traveler,</p>
                        <p>Your trip to <strong>%s</strong> has been <strong>booked</strong> successfully.</p>
                        <p>To confirm your travel, please complete the payment as soon as possible.</p>
                        <table style="width:100%%; border-collapse:collapse; margin:15px 0;">
                          <tr><td><strong>Destination:</strong></td><td>%s</td></tr>
                          <tr><td><strong>Travel Dates:</strong></td><td>%s → %s</td></tr>
                          <tr><td><strong>Amount Due:</strong></td><td>%s %.2f</td></tr>
                          <tr><td><strong>Status:</strong></td><td style="color:#f59e0b;">Awaiting Payment ⏳</td></tr>
                        </table>
                        <div style="text-align:center; margin-top:25px;">
                          <a href="http://localhost:5173/my-trips" style="background:#4f46e5; color:white; padding:10px 18px; border-radius:6px; text-decoration:none;">Complete Payment</a>
                        </div>
                        <p style="margin-top:20px; font-size:13px; color:#555;">
                          ⚠ <strong>Note:</strong> Cancelled trips will be automatically removed from your SmartTrip dashboard after 24 hours.
                        </p>
                      </div>
                      <div style="background:#f9fafb; padding:15px; text-align:center; font-size:13px; color:#666;">
                        <p>Thank you for planning your journey with <strong>SmartTrip</strong> 🌍</p>
                        <p>© 2025 SmartTrip Inc. All Rights Reserved.</p>
                      </div>
                    </div>
                  </div>
              """.formatted(destination, startDate, endDate, currency, amount);

            sendGridClient.sendEmail(email, subject, html);
            System.out.println("🧳 Pending payment email sent");

        } catch (Exception e) {
            System.err.println("⚠ Pending payment email failed: " + e.getMessage());
        }
    }
}
