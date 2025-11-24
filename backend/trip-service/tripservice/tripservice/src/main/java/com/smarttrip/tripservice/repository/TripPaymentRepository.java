package com.smarttrip.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smarttrip.tripservice.model.TripPayment;

import java.util.Optional;

@Repository
public interface TripPaymentRepository extends JpaRepository<TripPayment, Long> {

    Optional<TripPayment> findByOrderId(String orderId);

    Optional<TripPayment> findTopByTripIdOrderByPaymentDateDesc(Long tripId); // ✅ Fixed
}