package com.smarttrip.tripservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TripPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tripId;           // ✅ link to trip_plan.id
    private String userEmail;
    private String orderId;
    private double amount;
    private String currency;
    private String paymentStatus;  // PENDING, SUCCESS, FAILED
    private String paymentMethod;  // CARD, UPI, NETBANKING, etc.
    private LocalDateTime paymentDate; // when payment done
    
    

}

