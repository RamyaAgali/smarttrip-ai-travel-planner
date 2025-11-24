package com.smarttrip.tripservice.controller;

import com.smarttrip.tripservice.model.TripPayment;
import com.smarttrip.tripservice.model.TripPlan;
import com.smarttrip.tripservice.repository.TripPaymentRepository;
import com.smarttrip.tripservice.repository.TripPlanRepository;
import com.smarttrip.tripservice.service.CashfreeService;
import com.smarttrip.tripservice.service.EmailService;
import com.smarttrip.tripservice.util.InvoiceGenerator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:5174", "http://localhost:3000"})
public class PaymentController {

    private final TripPaymentRepository paymentRepo;
    private final TripPlanRepository tripRepo;
    private final EmailService emailService;
    private final CashfreeService cashfreeService;

    public PaymentController(TripPaymentRepository paymentRepo,
                             TripPlanRepository tripRepo,
                             EmailService emailService,
                             CashfreeService cashfreeService) {
        this.paymentRepo = paymentRepo;
        this.tripRepo = tripRepo;
        this.emailService = emailService;
        this.cashfreeService = cashfreeService;
    }

    /**
     * 🔹 Step 1: Create Payment (uses real user details via AuthService)
     */
    @PostMapping("/create")
    public Mono<Map<String, Object>> createPayment(@RequestBody Map<String, Object> payload) {
        Long tripId = Long.parseLong(payload.get("tripId").toString());
        Double amount = Double.parseDouble(payload.get("amount").toString());
        String currency = payload.get("currency").toString();
        //String email = payload.get("email").toString(); // ✅ use username instead of email
     // ✅ Use username if available, fallback to email, and make sure it’s Cashfree-safe
        Object u = payload.get("username") != null ? payload.get("username") : payload.get("email");
        String email = (u == null) ? "user" : u.toString();

        // Cashfree requires alphanumeric customer_id, so clean up email
        String safeCustomerId = email.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        String orderId = "ORDER-" + System.currentTimeMillis();

        // Save initial pending payment record
        TripPayment payment = TripPayment.builder()
                .tripId(tripId)
                .userEmail(email) // ✅ keeping this column name but storing username
                .amount(amount)
                .currency(currency)
                .paymentStatus("PENDING")
                .paymentMethod("N/A")
                .orderId(orderId)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepo.save(payment);

        // ✅ Create Cashfree order using CashfreeService (fetches real user details from AuthService)
        return cashfreeService.createOrder(orderId, amount, currency, safeCustomerId)
                .map(res -> {
                    System.out.println("✅ Cashfree Order Created: " + res);
                    return Map.of(
                            "orderId", orderId,
                            "paymentId", payment.getId(),
                            "cashfreeResponse", res
                    );
                });
    }

    /**
     * 🔹 Step 2: Confirm Payment + Generate & Email Invoice
     */
    @PostMapping("/success")
    public TripPayment confirmPayment(@RequestBody Map<String, Object> payload) {
        String orderId = payload.get("orderId").toString();
        String method = payload.get("method").toString();

        TripPayment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            System.out.println("⚠ Skipping duplicate success email (already processed) for Order ID: " + orderId);
            return payment;
        }

        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentMethod(method);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepo.save(payment);

        TripPlan trip = tripRepo.findById(payment.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        trip.setStatus("Paid");
        tripRepo.save(trip);

        try {
            // 🔹 Generate PDF invoice
            byte[] pdfBytes = InvoiceGenerator.generateInvoice(
                    trip.getUserEmail(),
                    trip.getDestination(),
                    trip.getStartDate(),
                    trip.getEndDate(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getPaymentMethod(),
                    payment.getOrderId()
            );

            // 🔹 Email invoice to user
            emailService.sendPaymentConfirmation(
                    trip.getUserEmail(),
                    trip.getDestination(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    trip.getStartDate(),
                    trip.getEndDate(),
                    pdfBytes
            );

            System.out.println("✅ Invoice sent to " + trip.getUserEmail());
        } catch (Exception e) {
            System.err.println("⚠ Failed to send invoice: " + e.getMessage());
        }

        return payment;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleCashfreeWebhook(@RequestBody Map<String, Object> payload) {
    	System.out.println(" Full Webhook Payload: " + payload);
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null || !data.containsKey("order")) {
                System.err.println("⚠ Invalid webhook payload");
                return ResponseEntity.badRequest().body("Invalid payload");
            }

            Map<String, Object> order = (Map<String, Object>) data.get("order");
            String orderId = (String) order.get("order_id");
            Map<String, Object> payment = (Map<String, Object>) data.get("payment");
            String paymentStatus = (String) payment.get("payment_status");
            String paymentMethod = payment.containsKey("payment_method") ? payment.get("payment_method").toString() : "Unknown";

            TripPayment paymentRecord = paymentRepo.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));
            
            if ("SUCCESS".equalsIgnoreCase(paymentRecord.getPaymentStatus())) {
                System.out.println("⚠ Duplicate webhook ignored for already successful Order ID: " + orderId);
                return ResponseEntity.ok("Duplicate success ignored");
            }
            
            TripPlan trip = tripRepo.findById(paymentRecord.getTripId())
                    .orElseThrow(() -> new RuntimeException("Trip not found for payment"));

            if ("PAID".equalsIgnoreCase(paymentStatus) || "SUCCESS".equalsIgnoreCase(paymentStatus)) {
                // ✅ Successful payment
                paymentRecord.setPaymentStatus("SUCCESS");
                paymentRecord.setPaymentMethod(paymentMethod);
                paymentRecord.setPaymentDate(LocalDateTime.now());
                paymentRepo.save(paymentRecord);

                trip.setStatus("Paid");
                tripRepo.save(trip);

                // Send success email with invoice
                emailService.sendPaymentConfirmation(
                    trip.getUserEmail(),
                    trip.getDestination(),
                    paymentRecord.getAmount(),
                    paymentRecord.getCurrency(),
                    trip.getStartDate(),
                    trip.getEndDate(),
                    null // or invoice PDF bytes if available
                );

                System.out.println("✅ Payment successful for Order ID: " + orderId);
            } 
            else if ("FAILED".equalsIgnoreCase(paymentStatus) || "FAILURE".equalsIgnoreCase(paymentStatus)|| "CANCELLED".equalsIgnoreCase(paymentStatus) || "ERROR".equalsIgnoreCase(paymentStatus)) {
                // ❌ Failed or cancelled payment
                paymentRecord.setPaymentStatus("FAILED");
                paymentRepo.save(paymentRecord);

                emailService.sendPaymentFailure(
                    trip.getUserEmail(),
                    trip.getDestination(),
                    paymentRecord.getAmount(),
                    paymentRecord.getCurrency(),
                    "Transaction declined or cancelled"
                );

                System.out.println("⚠ Payment failed or cancelled for Order ID: " + orderId);
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            System.err.println("💥 Webhook error: " + e.getMessage());
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }
    @GetMapping("/failure")
    public ResponseEntity<Void> handlePaymentFailureRedirect(
            @RequestParam(value = "order_id", required = false) String orderId) {

        System.out.println("❌ Redirected from Cashfree after failed payment: " + orderId);
        // Optional: update status again if needed
        if (orderId != null) {
            paymentRepo.findByOrderId(orderId).ifPresent(payment -> {
                payment.setPaymentStatus("FAILED");
                paymentRepo.save(payment);
            });
        }

        // 🔹 Redirect user to frontend failure page
        return ResponseEntity
                .status(302)
                .header("Location", "http://localhost:5173/payment/failure?order_id=" + orderId)
                .build();
    }
 // ⭐ NEW: Check latest payment status for a trip
    @GetMapping("/status/{tripId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long tripId) {
        try {
            // Find latest payment record for this trip
            TripPayment payment = paymentRepo
                    .findTopByTripIdOrderByPaymentDateDesc(tripId)
                    .orElse(null);

            if (payment == null) {
                return ResponseEntity.ok(Map.of(
                        "status", "NO_PAYMENT"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", payment.getPaymentStatus(),
                    "orderId", payment.getOrderId(),
                    "method", payment.getPaymentMethod(),
                    "amount", payment.getAmount(),
                    "currency", payment.getCurrency(),
                    "paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }
}