package com.eventpro.dto;

import com.eventpro.entity.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PaymentResponse(
        Long id,
        String reference,
        Long reservationId,
        String idempotencyKey,
        BigDecimal amount,
        Instant paymentDate,
        PaymentStatus status
) {
}

