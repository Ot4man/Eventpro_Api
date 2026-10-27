
package com.eventpro.repository;

import com.eventpro.entity.Refund;
import com.eventpro.entity.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByStatus(RefundStatus status);
}
