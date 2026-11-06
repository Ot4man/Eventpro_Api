package com.eventpro.scheduler;

import com.eventpro.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationExpirationScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedRate = 60000)
    public void releaseExpiredReservations() {
        int count = reservationService.expirePendingReservations();
        if (count > 0) {
            log.info("{} réservation(s) expirée(s) et places libérées avec succès.", count);
        }
    }
}
