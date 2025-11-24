package com.smarttrip.tripservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smarttrip.tripservice.model.TripPlan;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long>{
	List<TripPlan> findByUserEmail(String userEmail);
	List<TripPlan> findByStatusAndCancelledAtBefore(String status, LocalDateTime time);

}
