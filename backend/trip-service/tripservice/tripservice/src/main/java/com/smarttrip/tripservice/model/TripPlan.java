package com.smarttrip.tripservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "trip_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destination;
    private int days;
    private String currency;

    @Column(columnDefinition = "TEXT")
    private String planData;   // Full JSON plan from /api/travel/plan

    private String status;     // e.g., Planned, Booked, Completed, Cancelled

    // 🧭 Travel duration details
    private String startDate;  // YYYY-MM-DD
    private String endDate;    // YYYY-MM-DD

    // 🧑‍💼 User info (to track who booked)
    private String userEmail;

    // 🕒 Optional audit data
    private String createdAt;
    private Double totalCost;
    
    private LocalDateTime cancelledAt;
    
    private double destLat;
    private double destLon;
    private String travelMode;
    
}
