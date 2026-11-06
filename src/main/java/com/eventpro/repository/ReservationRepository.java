package com.eventpro.repository;

import com.eventpro.entity.Reservation;
import com.eventpro.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReference(String reference);

    List<Reservation> findByClientId(Long clientId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByStatusAndExpirationDateBefore(ReservationStatus status, Instant expirationDate);

    boolean existsByReference(String reference);
}
