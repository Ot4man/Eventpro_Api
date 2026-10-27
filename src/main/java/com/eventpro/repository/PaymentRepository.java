
package com.eventpro.repository;

import com.eventpro.entity.Payment;
import com.eventpro.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReference(String reference);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByReservationId(Long reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
