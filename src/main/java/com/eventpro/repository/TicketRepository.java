
package com.eventpro.repository;

import com.eventpro.entity.Ticket;
import com.eventpro.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByReservationLineReservationId(Long reservationId);

    boolean existsByCode(String code);
}
