package com.eventpro.repository;

import com.eventpro.entity.Reservation;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.entity.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
SELECT  r FROM Reservation r
JOIN r.client c
JOIN r.lines rl
JOIN rl.category
JOIN rl.category.category
WHERE r.status = :status
AND c.email = :email
AND rl.category.category.name = :nomCategorie
AND rl.quantity >= :quantiteMin""")


    List<Reservation> rechercherReservations(
            @Param("status") ReservationStatus status1,
            @Param("email")  String email,
            @Param("nomcategorie")  String nomCategorie,
            @Param("quantiteMin") int quantiteMin
    );

}

