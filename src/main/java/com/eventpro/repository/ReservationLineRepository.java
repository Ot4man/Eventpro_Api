
package com.eventpro.repository;

import com.eventpro.entity.ReservationLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationLineRepository
        extends JpaRepository<ReservationLine, Long> {

    List<ReservationLine> findByReservationId(Long reservationId);

    List<ReservationLine> findByCategoryId(Long categoryId);
}
