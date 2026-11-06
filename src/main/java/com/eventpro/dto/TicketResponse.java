package com.eventpro.dto;

import com.eventpro.entity.enums.TicketStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record TicketResponse(
        Long id,
        String code,
        Long reservationId,
        String eventTitle,
        String categoryName,
        BigDecimal price,
        String clientName,
        TicketStatus status,
        Instant issueDate,
        Instant scanDate
) {
}

