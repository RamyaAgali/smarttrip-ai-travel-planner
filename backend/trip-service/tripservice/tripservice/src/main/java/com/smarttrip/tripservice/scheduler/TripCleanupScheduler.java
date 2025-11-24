//package com.smarttrip.tripservice.scheduler;
//
//import java.time.LocalDateTime;
//
//import java.util.List;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.smarttrip.tripservice.model.TripPlan;
//import com.smarttrip.tripservice.repository.TripPlanRepository;
//
//@Component
//public class TripCleanupScheduler {
//	private final TripPlanRepository tripRepo;
//	
//	public TripCleanupScheduler(TripPlanRepository tripRepo) {
//		this.tripRepo = tripRepo;
//	}
//	
//	//Runs every time
//	@Scheduled(cron = "0 0 0 * * ?")
//	public void deleteExpiredCancelledTrips() {
//		LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
//		List<TripPlan> oldCancelledTrips = tripRepo.findByStatusAndCancelledAtBefore("Cancelled", cutoff);
//		
//		if(oldCancelledTrips.isEmpty()) {
//			tripRepo.deleteAll(oldCancelledTrips);
//			System.out.println(" Deleted " + oldCancelledTrips.size() + " old cancelled trips ");
//		}
//	}
//
//}
package com.smarttrip.tripservice.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smarttrip.tripservice.model.TripPlan;
import com.smarttrip.tripservice.repository.TripPlanRepository;

@Component
public class TripCleanupScheduler {

    private final TripPlanRepository tripRepo;

    public TripCleanupScheduler(TripPlanRepository tripRepo) {
        this.tripRepo = tripRepo;
    }

    // 🔁 For testing, run every 1 minute
     @Scheduled(fixedRate = 60000)

    // 🕒 For production, run daily at midnight
    //@Scheduled(cron = "0 0 0 * * ?")
    public void deleteExpiredCancelledTrips() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<TripPlan> oldCancelledTrips = tripRepo.findByStatusAndCancelledAtBefore("Cancelled", cutoff);

        if (!oldCancelledTrips.isEmpty()) {
            tripRepo.deleteAll(oldCancelledTrips);
            System.out.println("🗑 Deleted " + oldCancelledTrips.size() + " old cancelled trips");
        } else {
            System.out.println("✅ No expired cancelled trips found at this time.");
        }
    }
}
