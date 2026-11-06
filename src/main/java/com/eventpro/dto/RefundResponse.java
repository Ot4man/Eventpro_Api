package com.eventpro.dto;

import com.eventpro.entity.enums.RefundStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record RefundResponse(
        Long id,
        Long paymentId,
        BigDecimal amount,
        String reason,
        Instant requestDate,
        RefundStatus status,
        String message
) {
}

