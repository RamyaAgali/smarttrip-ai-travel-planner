package com.smarttrip.tripservice.controller;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarttrip.tripservice.model.TripPayment;
import com.smarttrip.tripservice.model.TripPlan;
import com.smarttrip.tripservice.repository.TripPaymentRepository;
import com.smarttrip.tripservice.repository.TripPlanRepository;
import com.smarttrip.tripservice.service.EmailService;


@RestController
@RequestMapping("/api/trip")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class TripPlanController {
	
	private final TripPlanRepository tripPlanRepository;
	private final TripPaymentRepository tripPaymentRepository;
	private final EmailService emailService;
	
	public TripPlanController(TripPlanRepository tripPlanRepository,TripPaymentRepository tripPaymentRepository,EmailService emailService) {
		this.tripPlanRepository = tripPlanRepository;
		this.emailService = emailService;
		this.tripPaymentRepository = tripPaymentRepository;
	}
	
	//Save a trip plan
	@PostMapping("/save")
	public TripPlan saveTrip(@RequestBody TripPlan trip) {
		return tripPlanRepository.save(trip);
		
	}
	
	//Get all trips for a user
	@GetMapping("/user/{email}")
	public List<TripPlan> getTripsByUser(@PathVariable String email){
		return tripPlanRepository.findByUserEmail(email);
	}
	
	// ✅ Mark as booked
	@PutMapping("/book/{id}")
	public TripPlan bookTrip(@PathVariable Long id) {
	    TripPlan trip = tripPlanRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Trip plan not found"));
	    trip.setStatus("Booked");
	    TripPlan savedTrip = tripPlanRepository.save(trip);
	    
	    // Send trip booked ( Pending payment) email
	    emailService.sendTripBookedPendingPayment(
	    		savedTrip.getUserEmail(),
	    		savedTrip.getDestination(),
	    		savedTrip.getStartDate(),
	    		savedTrip.getEndDate(),
	    		savedTrip.getTotalCost(),
	    		savedTrip.getCurrency()
	    		);
	    System.out.println(" Trip booked Email sent to: " + savedTrip.getUserEmail());
	    return tripPlanRepository.save(trip);
	}

	@PutMapping("/cancel/{id}")
	public ResponseEntity<?> cancelTrip(@PathVariable Long id) {
	    try {
	        TripPlan trip = tripPlanRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Trip not found"));

	        // ✅ Step 1: Mark trip as cancelled
	        trip.setStatus("Cancelled");
	        trip.setCancelledAt(LocalDateTime.now());
	        tripPlanRepository.save(trip);

	        // ✅ Step 2: Try to find a related payment
	        var optionalPayment = tripPaymentRepository.findTopByTripIdOrderByPaymentDateDesc(id);

	        if (optionalPayment.isPresent()) {
	            TripPayment payment = optionalPayment.get();

	            if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus()) || "PAID".equalsIgnoreCase(payment.getPaymentStatus())) {
	                payment.setPaymentStatus("REFUND_INITIATED");
	                tripPaymentRepository.save(payment);

	                // ✅ Send refund confirmation email
	                emailService.sendRefundInitiated(
	                        trip.getUserEmail(),
	                        trip.getDestination(),
	                        payment.getAmount(),
	                        payment.getCurrency(),
	                        payment.getOrderId()
	                );

	                System.out.println("💸 Refund initiated and email sent for Order ID: " + payment.getOrderId());
	            } else {
	                System.out.println("⚠ Payment record found but not successful. No refund needed. sending normal cancellation email");
	                emailService.sendTripCancellation(
	                		trip.getUserEmail(),
	                		trip.getDestination(),
	                		trip.getStartDate(),
	                		trip.getEndDate()
	                		);
	            }

	        } else {
	            // ✅ Step 3: No payment found → send trip cancellation email
	            emailService.sendTripCancellation(
	                    trip.getUserEmail(),
	                    trip.getDestination(),
	                    trip.getStartDate(),
	                    trip.getEndDate()
	            );
	            System.out.println("📨 Trip cancellation email sent for unpaid trip ID: " + id);
	        }

	        return ResponseEntity.ok("Trip cancelled successfully");

	    } catch (Exception e) {
	        System.err.println("💥 Error cancelling trip: " + e.getMessage());
	        return ResponseEntity.status(500).body("Cancellation failed: " + e.getMessage());
	    }
	}
	@GetMapping("/{id}")
	public ResponseEntity<TripPlan> getTripById(@PathVariable Long id){
		return tripPlanRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/rebook/{id}")
	public ResponseEntity<?> rebookTrip(@PathVariable Long id) {
	    try {
	        TripPlan trip = tripPlanRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Trip not found"));

	        if (!"Cancelled".equalsIgnoreCase(trip.getStatus())) {
	            return ResponseEntity.badRequest().body("Trip is not cancelled");
	        }

	        // Check if within 24 hours
	        if (trip.getCancelledAt() == null ||
	            trip.getCancelledAt().isBefore(LocalDateTime.now().minusHours(24))) {
	            return ResponseEntity.badRequest().body("Rebooking window expired (24 hours passed)");
	        }

	        // ✅ Find latest payment (if any)
	        var optionalPayment = tripPaymentRepository.findTopByTripIdOrderByPaymentDateDesc(id);
	        boolean paymentExists = optionalPayment.isPresent();
	        String messageNote;

	        // ✅ Step 1: Reset trip to Booked
	        trip.setStatus("Booked");
	        trip.setCancelledAt(null);
	        tripPlanRepository.save(trip);

	        // ✅ Step 2: Email user based on payment history
	        if (paymentExists) {
	            TripPayment payment = optionalPayment.get();
	            if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus()) ||
	                "PAID".equalsIgnoreCase(payment.getPaymentStatus()) ||
	                "REFUND_INITIATED".equalsIgnoreCase(payment.getPaymentStatus())) {
	                
	                messageNote = "Your trip has been rebooked successfully! Please complete a new payment to confirm your rebooking.";
	            } else {
	                messageNote = "Your trip has been rebooked successfully! Please complete the payment to confirm your trip.";
	            }
	        } else {
	            messageNote = "Your trip has been rebooked successfully! Please complete payment to confirm your trip.";
	        }

	        // ✅ Send rebook confirmation email
	        emailService.sendTripRebooked(
	                trip.getUserEmail(),
	                trip.getDestination(),
	                trip.getStartDate(),
	                trip.getEndDate()
	        );

	        System.out.println("✅ Trip rebooked successfully and email sent to " + trip.getUserEmail());
	        return ResponseEntity.ok(messageNote);

	    } catch (Exception e) {
	        System.err.println("💥 Error during rebooking: " + e.getMessage());
	        return ResponseEntity.status(500).body("Rebooking failed: " + e.getMessage());
	    }
	}
	
}
